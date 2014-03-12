/**
 * 
 */
package com.chamago.cometserver.connection;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.chamago.cometserver.PullEvent;
import com.chamago.cometserver.StreamConstants;

/**
 * @author Gavin.peng
 * 
 *         2014-2-24 下午03:08:15 × cometserver
 */
public class CometConnectionManager {

	private final static Log LOG = LogFactory
			.getLog(CometConnectionManager.class);

	private volatile boolean containExit = false;
	
	private ConcurrentHashMap<String, CometConnection> connectCollect;
	private TimeOutThread toThread;
	

	public CometConnectionManager() {
		this.init();
	}

	
	/**
	 * 注册请求连接,如果两个相同的客户端同时注册， 只有一个连接生效。即新的连接会踢掉老的连接,同时关闭老的连接
	 * 
	 * @param connect
	 */
	public void registerConnection(CometConnection connect) {
		String appKey = connect.getAppkey();
		// 同一个客户端重复的连接，则启用新的连接
		if (this.connectCollect.containsKey(appKey)) {
			LOG.info("系统接收到一个新的客户端连接[" + connect.getAppkey()
					+ "],同时发现该客户端已经建立了连接，踢掉老的连接");
			PullEvent hb = new PullEvent(connect.getAppkey(),StreamConstants.CLIENT_KICKOFF,null,"相同的appkey已经建立了连接，退换掉老的连接");
			connect.pullMessage(hb);
			CometConnection oldConnect = this.connectCollect.get(appKey);
			oldConnect.closeConnection();
			this.connectCollect.remove(appKey);
			this.connectCollect.put(appKey, connect);
		} else {
			LOG.info("系统接收到一个新的客户端连接[" + connect.getAppkey() + "]");
			this.connectCollect.put(appKey, connect);
		}
		//processDiscardMsg(connect);
	}

	private void init() {
		this.connectCollect = new ConcurrentHashMap<String, CometConnection>(50);
		this.toThread = new TimeOutThread("Connect-Monitor-Thread");
		this.toThread.start();
	}
	
	
	/**
	 * 根据appkey找到链接
	 * @param appkey
	 * @return
	 */
	public CometConnection findCometConnection(String appkey){
		return this.connectCollect.get(appkey);
	}
	
	/**
	 * 根据appkey从链接集合中移除
	 * @param appkey
	 * @return
	 */
	public void closeCometConnection(String appkey){
		CometConnection connect = this.connectCollect.remove(appkey);
		connect = null;
	}


	public int getConnectSize(){
		return this.connectCollect.size();
	}
	
	
	/**
	 * 踢掉所有链接,并发消息告诉所有客户端，让其在一定的时间重连，在服务端更新时会用，
	 */
	public void killAllConnection(){
		//停止心跳线程，防止连接关闭了，还写心跳消息。
		stopMonitorThread();
		LOG.info("服务端升级，关闭所有客户端连接....");
		Iterator<String> connectIts = CometConnectionManager.this.connectCollect
		.keySet().iterator();
		while (connectIts.hasNext()) {
			String connectKey = connectIts.next();
			CometConnection connect = CometConnectionManager.this.connectCollect
					.get(connectKey);
			PullEvent hb = new PullEvent(connect.getAppkey(),StreamConstants.SERVER_DEPLOY,null,"服务端在升级，客户端请稍后重连!");
			connect.pullMessage(hb);
			connect.closeConnection();
		}
	}
	
	public void processDiscardMsg(final CometConnection connetion){
		connetion.streamMsgPullFactory.commit(new Runnable(){
			@Override
			public void run() {
				RedisClientManager rcm = RedisClientManager.getInstance();
				Map<String,String> msgMap = rcm.findDiscardMsg(connetion.getAppkey());
				if(msgMap!=null&&msgMap.size()>0){
					Iterator<String> it = msgMap.keySet().iterator();
					List<PullEvent> peList = new ArrayList<PullEvent>();
					while(it.hasNext()){
						String msgId = it.next();
						String content = msgMap.get(msgId);
						PullEvent pe = new PullEvent(connetion.getAppkey(),StreamConstants.DISCARD_MESSAGE,null,content);
						peList.add(pe);
					}
					LOG.info("发现客户端连接["+connetion.getAppkey()+"]丢弃的消息数为:"+peList.size());
					((StreamCometConnection)connetion).batchPullEvent(peList);
				}
				
			}
			
		});
		
	}
	
	public boolean isContainExit() {
		return containExit;
	}


	public void setContainExit(boolean containExit) {
		this.containExit = containExit;
	}


	public void clear(){
		this.connectCollect.clear();
		this.connectCollect = null;
		
	}
	
	public void stopMonitorThread(){
		this.toThread.stopThread();
	}
	public class TimeOutThread extends Thread {

		private boolean running = false;
		private long sleepTime = 30 * 1000;

		public TimeOutThread(String name) {
			this.setName(name); 
			this.running = true;
		}

		public void run() {

			while (running) {

				long nowTime = System.currentTimeMillis();
				Iterator<String> connectIts = CometConnectionManager.this.connectCollect
						.keySet().iterator();
				LOG.info("系统维持的连接数为:"+CometConnectionManager.this.connectCollect.size());
				while (connectIts.hasNext()) {
					String connectKey = connectIts.next();
					CometConnection connect = CometConnectionManager.this.connectCollect
							.get(connectKey);
					long connectTime = nowTime - connect.getConnectionTime();
					if (connectTime > StreamConstants.CONNECT_MAX_TIME) {
						LOG.info("客户端连接[" + connectKey + "] 连接时间 是["
								+ connectTime / (1000 * 60 * 60)
								+ "]小时,达到连接的最大有效时间24小时，系统将主动关闭该连接，客户端需要重连");
						PullEvent hb = new PullEvent(connect.getAppkey(),StreamConstants.CONNECT_REACH_MAX_TIME,null,"connect is reached max time 24h");
						connect.pullMessage(hb);
						connect.closeConnection();
					}
					//检查30s内是否有下发数据，没有则发送心跳消息，防止超时
					if((nowTime-connect.getActiveTime())>=StreamConstants.PARAM_HEARBEAT_TIME){
						if(connect.needWorkFlag.get()){
							PullEvent hb = new PullEvent(connect.getAppkey(),StreamConstants.HEAT_BEAT,null,"heatbeak msg");
							connect.pullMessage(hb);
						}
					}
				}
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			LOG.info("监控线程"+this.getName()+"退出");

		}

		public void stopThread() {
			this.running = false;
			interrupt();
		}

	}

}
