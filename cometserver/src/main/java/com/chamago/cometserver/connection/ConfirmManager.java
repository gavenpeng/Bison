/**
 * 
 */
package com.chamago.cometserver.connection;

import com.chamago.cometserver.LinkListQueue;
import com.chamago.cometserver.PullEvent;

/**
 * @author Gavin.peng
 * 
 * 2014-3-4 下午04:03:58
 × cometserver
 */
public class ConfirmManager {
	
	//待推送消息队列
	protected LinkListQueue<String> firmQueue;
	
	public ConfirmManager(){
		this.firmQueue = new LinkListQueue<String>();
	}
	
	public void collectMsgId(String msgId){
		this.firmQueue.offer(msgId);
	}
}
