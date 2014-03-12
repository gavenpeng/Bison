/**
 * 
 */
package com.chamago.cometserver;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Gavin.peng
 * 
 * 2014-2-24 下午06:35:23
 × cometserver
 */
public class PullEvent {
	
	private String appkey;
	private String subject;
	private String msg;
	private String code;
	private String id;
	
	private static final AtomicInteger MESSAGE_KEY = new AtomicInteger(0);
	private int key = MESSAGE_KEY.incrementAndGet();
	  
	private final static String ENTER_CHARS = "\r\n";
	
	
	public PullEvent(String appkey,String code,String subject,String msg){
		this.appkey = appkey;
		this.code = code;
		this.subject = subject;
		this.msg = msg;
	}
	
	public String getAppkey() {
		return appkey;
	}
	public void setAppkey(String appkey) {
		this.appkey = appkey;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
	public String getId() {
		
		return System.currentTimeMillis()+String.valueOf(key);
	}

	

	@Override
	public String toString(){
		StringBuilder msg = new StringBuilder("{\"packet\":{");
		msg.append("\"code\":");
		msg.append(this.code);
		msg.append(",");
		msg.append("\"confirm_id\":");
		msg.append(this.getId());
		msg.append(",");
		msg.append("\"msg\":");
		msg.append(this.msg);
		msg.append("}}").append(ENTER_CHARS);
		return  msg.toString();
	}
	
	public static void main(String[] args){
		System.out.println(System.currentTimeMillis());
	}
	
}
