/**
 * 
 */
package com.chamago.cometserver.connection;

import java.util.List;
import java.util.Map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @author Gavin.peng
 * 
 * Redis 通讯的客户端工具类
 * 
 * 2014-3-4 下午05:10:16
 × cometserver
 */
public class RedisClientManager {
	
	private static RedisClientManager instance = new RedisClientManager();
	
	private RedisClientManager(){
		
	}
	
	public static RedisClientManager getInstance(){
		return instance;
	}
	
	/**
	 * 删除redis上，指定客户端对应的消息
	 * @param appkey
	 * @param msgId
	 */
	public void deleteMsg(String appkey,String msgId){
		
		JedisPool pool = RedisClientFactory.getJedisPool();
		Jedis jedis = pool.getResource();  
		try {
			//从与Key关联的Set中删除参数中指定的成员，不存在的参数成员将被忽略，如果该Key并不存在，将视为空Set处理。
			//void srem(final String key, final String... members)
			//从指定Key的Hashes Value中删除参数中指定的多个字段，如果不存在的字段将被忽略。如果Key不存在，则将其视为空Hashes，并返回0.返回实际删除的Field数量。
			//void hdel(final String key, final String... fields)
			//删除指定客户端连接对应的消息，即客户端已经收到消息。
			jedis.hdel(appkey, msgId);
		 
		} finally {  
		  //这里很重要，一旦拿到的jedis实例使用完毕，必须要返还给池中  
		  pool.returnResource(jedis);  
		}  
	}
	
	/**
	 * 批量删除redis上，指定客户端对应的消息
	 * @param appkey
	 * @param msgIds
	 */
	public void batchDeleteMsg(String appkey,String msgIds){
		
		JedisPool pool = RedisClientFactory.getJedisPool();
		Jedis jedis = pool.getResource();  
		try {
			//从与Key关联的Set中删除参数中指定的成员，不存在的参数成员将被忽略，如果该Key并不存在，将视为空Set处理。
			//void srem(final String key, final String... members)
			//从指定Key的Hashes Value中删除参数中指定的多个字段，如果不存在的字段将被忽略。如果Key不存在，则将其视为空Hashes，并返回0.返回实际删除的Field数量。
			//void hdel(final String key, final String... fields)
			//删除指定客户端连接对应的消息，即客户端已经收到消息。
			//jedis.hdel(appkey, msgId);
			String[] fields = msgIds.split(",");
			jedis.hdel(appkey, fields);
		 
		} finally {  
		  //这里很重要，一旦拿到的jedis实例使用完毕，必须要返还给池中  
		  pool.returnResource(jedis);  
		}  
	}
	
	/**
	 * 保存消息到redis上，关系为，一个客户端对应一个map，消息id为key，
	 * value为消息的内容。
	 * @param appkey
	 * @param msgId
	 * @param msg
	 */
	public void saveMsg(String appkey,String msgId,String msg){
		//先写到redis，
		JedisPool pool = RedisClientFactory.getJedisPool();
		Jedis jedis = pool.getResource();
		try {
			//为指定的Key设定Field/Value对，如果Key不存在，
			//该命令将创建新Key以参数中的Field/Value对，如果参数中的Field在该Key中已经存在，
			//则用新值覆盖其原有值。
			jedis.hset(appkey,msgId,msg);
			//jedis.hsetnx(key, field, value);
		} finally {  
		  //这里很重要，一旦拿到的jedis实例使用完毕，必须要返还给池中  
		  pool.returnResource(jedis);  
		}  
	}
	
	/**
	 * 根据appkey查找对应的消息队列
	 * @param appkey
	 * @return
	 */
	public Map<String,String> findDiscardMsg(String appkey){
		JedisPool pool = RedisClientFactory.getJedisPool();
		Jedis jedis = pool.getResource();
		try {
			//获取该键包含的所有Field/Value。其返回格式为一个Field、一个Value，并以此类推。
			Map<String,String> msgMap = jedis.hgetAll(appkey);
			return msgMap;
			
		} finally {  
		  //这里很重要，一旦拿到的jedis实例使用完毕，必须要返还给池中  
		  pool.returnResource(jedis);  
		} 
		
	}

}
