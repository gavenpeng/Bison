/**
 * 
 */
package com.chamago.cometserver.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;



/**
 * @author Gavin.peng
 * 2013-1-5
 */
public class JdbcManager{
	private static String driver="com.mysql.jdbc.Driver";
	private static String url="jdbc:mysql://cmg3.chamago.com:3306/open?useUnicode=true&characterEncoding=utf8&autoReconnect=true&failOverReadOnly=false&useOldAliasMetadataBehavior=true";
	//private static String urlEops="jdbc:mysql://yamato.chamago.com:3306/eops?useUnicode=true&characterEncoding=utf8&autoReconnect=true&failOverReadOnly=false";
	private static String user="gavin.peng";
	private static String password="gavin.peng";
	
	static{
		try {
			
			 InputStream is = JdbcManager.class.getClassLoader().getResourceAsStream ("config.properties");
			 Properties props=new Properties();
			 props.load(is);
			 String cUrl = props.getProperty("open.datasource.url");
			 String cName = props.getProperty("open.datasource.username");
			 String cPassword = props.getProperty("open.datasource.password");
			 if(cUrl!=null){
				 url = cUrl;
			 }
			 if(cName!=null){
				 user = cName;
			 }
			 if(cPassword!=null){
				 password = cPassword;
			 }
			 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static Connection getConnection(String url,String user,String password){
		  Connection conn = null;
		    try{
		    	Class.forName(driver);
		    	conn = DriverManager.getConnection(url, user,password);
				return conn;
		    }catch(Exception e){
		    	e.printStackTrace();
		    }
		    return null;
	}
	
	public static Connection getConnection(){
		  Connection conn = null;
		    try{
		    	Class.forName(driver);
		    	conn = DriverManager.getConnection(url, user,password);
				return conn;
		    }catch(Exception e){
		    	e.printStackTrace();
		    }
		    return null;
	}
	
	
	public static Map<String,String> getEhubAllAppkey() throws Exception{
		String sql = "SELECT appkey,appsecret FROM ehub";
		Connection conn = getConnection();
		PreparedStatement pstat = null;
		ResultSet rs = null;
		try {
			pstat = conn.prepareStatement(sql);
			rs = pstat.executeQuery();
			Map<String,String> keys = new HashMap<String,String>();
			while (rs.next()) {
				String key = rs.getString(1);
				String secret = rs.getString(2);
				keys.put(key, secret);
			}
			return keys;
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (conn != null) {
				conn.close();
			}
			if (pstat != null) {
				pstat.close();
				pstat = null;
			}
			if (rs != null) {
				rs.close();
				rs = null;
			}
		}
	}
	
	
	public static String getSecretByAppkey(String appkey) throws Exception{
		String sql = "SELECT appsecret FROM ehub where appkey = ?";
		Connection conn = getConnection();
		PreparedStatement pstat = null;
		ResultSet rs = null;
		try {
			pstat = conn.prepareStatement(sql);
			pstat.setString(1,appkey);
			rs = pstat.executeQuery();
			Map<String,String> keys = new HashMap<String,String>();
			while (rs.next()) {
				String secret = rs.getString(2);
				return secret;
			}
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (conn != null) {
				conn.close();
			}
			if (pstat != null) {
				pstat.close();
				pstat = null;
			}
			if (rs != null) {
				rs.close();
				rs = null;
			}
		}
	}
	
		
}
