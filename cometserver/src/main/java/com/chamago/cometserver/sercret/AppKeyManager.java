/**
 * 
 */
package com.chamago.cometserver.sercret;

import java.util.HashMap;
import java.util.Map;

import com.chamago.cometserver.util.JdbcManager;

/**
 * @author Gavin.peng
 * 
 * 2014-3-5 上午11:25:08
 × cometserver
 */
public class AppKeyManager {
	
	private Map<String,String> keyMaps;
	
	private static AppKeyManager akm = new AppKeyManager();
	
	private AppKeyManager(){
		try {
			this.initAppKeys();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void initAppKeys() throws Exception{
		keyMaps = JdbcManager.getEhubAllAppkey();
	}
	
	public static AppKeyManager getInstance(){
		return akm;
	}
	
	public String findSecret(String appkey){
		String secret = this.keyMaps.get(appkey);
		if(secret !=null){
			return secret;
		}
		synchronized(this){
			if(this.keyMaps.get(appkey)!=null){
				return this.keyMaps.get(appkey);
			}
			try {
				secret = JdbcManager.getSecretByAppkey(appkey);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(secret !=null){
				this.keyMaps.put(appkey, secret);
			}
		}
		return secret;
	}
	
 
}
