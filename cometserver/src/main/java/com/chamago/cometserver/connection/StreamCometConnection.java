/**
 * 
 */
package com.chamago.cometserver.connection;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;

import org.eclipse.jetty.continuation.Continuation;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.chamago.cometserver.LinkListQueue;
import com.chamago.cometserver.PullEvent;
import com.chamago.cometserver.StreamConstants;

/**
 * @author Gavin.peng
 * 
 *         2014-2-21 下午03:13:51 × cometserver
 */
public class StreamCometConnection extends CometConnection {

	
	private CometConnectionManager connectionManager;
	
	
	
	public StreamCometConnection(StreamMsgPullFactory streamMsgPullFactory,CometConnectionManager connectionManager) {
		super(streamMsgPullFactory);
		this.connectionManager = connectionManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.chamago.cometserver.connection.CometConnection#pullMessage(java.lang
	 * .String)
	 */
	@Override
	void pullMessage(PullEvent event) {
		if(event.getCode().equals("100")){
			commitTask();
		}else if(event.getCode().equals(StreamConstants.HEAT_BEAT)
				||event.getCode().equals(StreamConstants.SERVER_DEPLOY)
				||event.getCode().equals(StreamConstants.CONNECT_REACH_MAX_TIME)
				||this.subs.contains(event.getSubject())){
			//先写到redis上
			if(event.getCode().equals(StreamConstants.NEW_MESSAGE)){
				RedisClientManager rcm = RedisClientManager.getInstance();
				rcm.saveMsg(event.getAppkey(), event.getId(), event.toString());
			}
			this.msgQueue.offer(event);
			commitTask();
		}else{
			LOG.info("连接通道["+this.getAppkey()+"],没有订阅["+event.getSubject()+"]主题事件,忽略该事件");
		}
	}

	/**
	 * 
	 * @param events
	 */
	public void batchPullEvent(List<PullEvent> events){
		if(events!=null&&events.size()>0){
			for(PullEvent pe:events){
				if(pe.getCode().equals(StreamConstants.HEAT_BEAT)
						||pe.getCode().equals(StreamConstants.SERVER_DEPLOY)
						||pe.getCode().equals(StreamConstants.DISCARD_MESSAGE)
						||this.subs.contains(pe.getSubject())){
					if(pe.getCode().equals(StreamConstants.NEW_MESSAGE)){
						RedisClientManager rcm = RedisClientManager.getInstance();
						rcm.saveMsg(pe.getAppkey(), pe.getId(), pe.toString());
					}
					this.msgQueue.offer(pe);
				}else{
					LOG.info("连接通道["+this.getAppkey()+"],没有订阅["+pe.getSubject()+"]主题事件,忽略该事件");
				}
			}
			commitTask();
		}
	}
	
	private void commitTask(){
		//如果该链接的消息队列没有下发任务，在创建一个rask
		if(this.needWorkFlag.compareAndSet(true,false)){
			PullTask pt = new PullTask(this);
			this.streamMsgPullFactory.commit(pt);
		}
	}

	public class PullTask implements Runnable {

		private CometConnection connect;

		public PullTask(CometConnection connect) {
			this.connect = connect;
		}

		@Override
		public void run() {
			
			ServletResponse resp = this.connect.getResponse();
			while(this.connect.msgQueue.size()>0){
					
					PullEvent msg = null;
					try {
						msg = this.connect.msgQueue.take();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					if(LOG.isDebugEnabled()){
						LOG.debug("向连接通道下发消息:"+msg.toString());
					}
					ServletOutputStream os = null;
					try {
						os = resp.getOutputStream();
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						os.write(msg.toString().getBytes());
						os.flush();
						
					} catch (Exception e) {
						LOG.warn("客户端连接["+this.connect.getAppkey()+"]出现异常，需要关闭该连接");
						//标记该链接通道上没有消息要发送。
						this.connect.msgQueue.clear();
						this.connect.needWorkFlag.compareAndSet(false,true);
						this.connect.closeConnection();
						e.printStackTrace();
						break;
					}
					this.connect.setActiveTime(System.currentTimeMillis());
			}
			//标记该链接通道上没有消息要发送。
			this.connect.needWorkFlag.compareAndSet(false,true);
			//通知关闭长连接的线程
			synchronized(this.connect){
				this.connect.notifyAll();
			}
		}

	}

	@Override
	void clean() {
		this.msgQueue = null;
		this.subs.clear();
		this.subs = null;
		Continuation continuation = this.getContinuation();
		//释放掉该链接的资源，关闭response
		continuation.complete();
		this.connectionManager.closeCometConnection(this.getAppkey());
		LOG.info("客户端连接["+this.getAppkey()+"]关闭,释放系统资源");
		synchronized(this.connectionManager){
			if(this.connectionManager.getConnectSize()<=0&&!this.connectionManager.isContainExit()){
				LOG.info("所有链接已经成功关闭，通知容器退出");
				this.connectionManager.setContainExit(true);
				this.connectionManager.notifyAll();
			}
		}
		
	}

}
