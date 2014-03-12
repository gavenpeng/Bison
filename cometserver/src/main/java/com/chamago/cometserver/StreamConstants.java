package com.chamago.cometserver;

/**
 * 
 * @author zhenzi
 * 2011-8-9 下午01:58:09
 */
public final class StreamConstants {
	public static final String ERR_MSG_HEADER = "errmsg";
	public static final String PARAM_APPKEY = "app_key";
	public static final String PARAM_SUBJECT = "subject";
	public static final String PARAM_CONTENT = "content";
	public static final String PARAM_USERID = "user";
	public static final String PARAM_CONNECT_ID = "id";
	public static final String PARAM_TIMESTAMP = "timestamp";
	public static final String PARAM_SIGN = "sign";
	public static final String PARAM_GROUP_ID = "group_id";
	public static final String PARAM_IS_RELIABLE = "is_reliable";
	
	public static final String PARAM_EXPIRED = "expired";
	
	//客户端连接保持的最大时间，默认为24小时
	public static final long CONNECT_MAX_TIME = 24*60*60*1000;
	//心跳时间间隔30s
	public static final long PARAM_HEARBEAT_TIME = 30*1000;
	
	//code
	public static final String CONNECT_SUCCESS = "200";//连接成功的code
	public static final String HEAT_BEAT = "201";//心跳
	public static final String NEW_MESSAGE = "202";//消息
	public static final String DISCARD_MESSAGE = "203";//当客户端断开连接后，服务端会记录下来丢弃消息的开始时间
	public static final String CONNECT_REACH_MAX_TIME = "101";//连接到达最大时间，服务端主动断开
	public static final String SERVER_DEPLOY = "102";//服务端在发布
	public static final String SERVER_REHASH = "103";//服务端负载不均衡了，断开所有的客户端重连
	public static final String CLIENT_KICKOFF = "104";//对于重复的连接，服务端用新的连接替换掉旧的连接
	public static final String SERVER_KICKOFF = "105";//由于消息量太大，而isv接收的速度太慢，服务端断开isv的连接
	
}
