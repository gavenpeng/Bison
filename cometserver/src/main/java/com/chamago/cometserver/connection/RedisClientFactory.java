/**
 * 
 */
package com.chamago.cometserver.connection;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author Gavin.peng
 * 
 * 2014-3-4 下午04:51:59
 × cometserver
 */
public class RedisClientFactory {
	
	
	private static JedisPool pool; 
	
	static{
		pool = new JedisPool(new JedisPoolConfig(), "192.168.1.141");
	}
	
	
	public static JedisPool getJedisPool(){
		return pool;
	}

	

}
