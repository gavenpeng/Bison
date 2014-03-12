/**
 * 
 */
package com.chamago.cometserver.connection;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.continuation.Continuation;

import com.chamago.cometserver.LinkListQueue;
import com.chamago.cometserver.PullEvent;
import com.chamago.cometserver.StreamConstants;
import com.chamago.cometserver.connection.StreamCometConnection.PullTask;

/**
 * @author Gavin.peng
 * 
 * 2014-2-21 下午02:51:54
 × cometserver
 */
public abstract class CometConnection {
	
	protected final static Log LOG = LogFactory
	.getLog(CometConnection.class);
	
	private String appkey;
	
	private String secret;
	//相当于主题，有ORDER,ITEM 等
	private String connectId;
	//目前暂时不用
	private String groupId;//
	//连接维持的时间,单位秒
	private long connectionTime;
	
	
	//待推送消息队列
	protected LinkListQueue<PullEvent> msgQueue;
	
	protected Set<String> subs;
	
	//标记该链接的消息队列是否有工作线程在下发消息 true 标示没有，false标示有
	protected AtomicBoolean needWorkFlag = new AtomicBoolean(true);
	
	private Long activeTime;
	
	protected Continuation continuation;
	
	private HttpServletResponse response;
	
	private HttpServletRequest request;
	
	protected StreamMsgPullFactory streamMsgPullFactory;
	
	public CometConnection(StreamMsgPullFactory streamMsgPullFactory){
		
		this.streamMsgPullFactory = streamMsgPullFactory;
		this.connectionTime = System.currentTimeMillis();
		this.msgQueue = new LinkListQueue<PullEvent>();
		this.subs = new HashSet<String>(5);
		this.activeTime = System.currentTimeMillis();
	}
	
	public String getAppkey() {
		return appkey;
	}
	public void setAppkey(String appkey) {
		this.appkey = appkey;
	}
	public String getSecret() {
		return secret;
	}
	public void setSecret(String secret) {
		this.secret = secret;
	}
	public String getConnectId() {
		return connectId;
	}
	public void setConnectId(String connectId) {
		this.connectId = connectId;
	}
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	
	public void addSubject(String subject){
		this.subs.add(subject);
	}
	
	public long getConnectionTime() {
		return connectionTime;
	}

	public void setConnectionTime(long connectionTime) {
		this.connectionTime = connectionTime;
	}

	public LinkListQueue<PullEvent> getMsgQueue() {
		return msgQueue;
	}

	public void setMsgQueue(LinkListQueue<PullEvent> msgQueue) {
		this.msgQueue = msgQueue;
	}

	
	public Continuation getContinuation() {
		return continuation;
	}

	public void setContinuation(Continuation continuation) {
		this.continuation = continuation;
	}

	
	public HttpServletResponse getResponse() {
		return response;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	
	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	/**
	 * 业务推送消息入口
	 * @param msg
	 */
	abstract void pullMessage(PullEvent event);
	
	public void holdingConnection(HttpServletResponse response,HttpServletRequest request){
		Continuation contin = this.getContinuation();
		this.response = response;
		this.request = request;
		contin.suspend(response);
	}
	
	
	/**
	 * 关闭连接
	 */
	public void closeConnection(){
		
		//说明该链接通道正在下发数据,需要挂取该线程，。
		if(!this.needWorkFlag.get()||(this.needWorkFlag.get()&&this.msgQueue.size()>0)){
			LOG.info("连接["+this.getAppkey()+"]还有下发线程在工作，或者链接下发队列还有消息需要发送，需要等待发送完");
			final CometConnection curcon = this;
			//这里异步是为了防止挂起管理连接的线程。
			new Thread(new Runnable(){
				@Override
				public void run() {
					if(!curcon.needWorkFlag.get()){
						synchronized(curcon){
							try {
								curcon.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}else if(curcon.needWorkFlag.get()&&curcon.msgQueue.size()>0){
						PullEvent sysEvent = new PullEvent(curcon.appkey,"100",null,null);
						curcon.pullMessage(sysEvent);
						synchronized(curcon){
							try {
								curcon.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
					clean();
				}
				
			}).start();
		}else{
			clean();
		}
	}
	
	abstract void clean();

	public long getActiveTime() {
		return activeTime;
	}

	public void setActiveTime(Long activeTime) {
		this.activeTime = activeTime;
	}
	
	
}
