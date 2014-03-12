package com.chamago.cometserver;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.chamago.cometserver.connection.CometConnectionManager;
import com.chamago.cometserver.connection.StreamMsgPullFactory;
import com.chamago.cometserver.util.JdbcManager;

public class CometServer {

	private final static Log LOG = LogFactory.getLog(CometServer.class);
	
	private Server jetty;
	private ServletHolder csHolder;
	private int default_port = 8770;
	
	public static boolean NO_DISCARD = false;
	
	public static int MIN_THREADS = 10;
	public static int MAX_THREADS = 15;
	public static int QUEUE_SIZE = 5000;
	
	
    public static void main(String[] args) {
    	
    	 new CometServer();
    }
    
    public CometServer(){
    	this.init();
    	this.addShutdownHook();
    	this.startJettyServer();
    }
    
    public void init(){
    		try {
    			
    			 InputStream is = JdbcManager.class.getClassLoader().getResourceAsStream ("config.properties");
    			 Properties props=new Properties();
    			 props.load(is);
    			 String port = props.getProperty("cometserver.http.port");
    			 if(port!=null){
    				 default_port = Integer.parseInt(port);
    			 }
    			 String mint = props.getProperty("pull.thread.pool.minthreads");
    			 if(mint!=null){
    				 MIN_THREADS = Integer.parseInt(mint);
    			 }
    			 String maxt = props.getProperty("pull.thread.pool.minthreads");
    			 if(maxt!=null){
    				 MAX_THREADS = Integer.parseInt(maxt);
    			 }
    			 String queuesize = props.getProperty("pull.thread.pool.queuesize");
    			 if(queuesize!=null){
    				 QUEUE_SIZE = Integer.parseInt(queuesize);
    			 }
    			 
    			 String discard = props.getProperty("cometserver.discard.on");
    			 if(discard!=null){
    				 NO_DISCARD = Boolean.parseBoolean(discard);
    			 }
    			 
    			 
    		} catch (FileNotFoundException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    		
    }
    
    public void startJettyServer(){
    	jetty = new Server(); 
   	    Connector connector = new SelectChannelConnector(); 
	   	connector.setPort(default_port);
	   	jetty.setConnectors(new Connector[]{ connector }); 
	   	ServletContextHandler root = new 
	   	ServletContextHandler(null,"/cometserver",ServletContextHandler.NO_SESSIONS); 
	   	csHolder = root.addServlet("com.chamago.cometserver.CometServlet","/cometser/rest"); 
	   	//设置在容器启动时就加载
	   	csHolder.setInitOrder(-1);
	   	root.addServlet("com.chamago.cometserver.ReceMsgServlet","/recvser/rest"); 
	   	root.addServlet("com.chamago.cometserver.ConfirmServlet","/confirmser/rest"); 
	   	jetty.setHandler(root);
	   	jetty.setStopAtShutdown(true);
	   	try {
	   		jetty.start();
	   		jetty.join();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }
    
    public void addShutdownHook(){
  	  Runtime.getRuntime().addShutdownHook(new Thread() {
  	      public void run() {
  	    	  stopService();
  	      	}
  	  });
    }
    
    public void stopService(){
    	
    	try {
    		//先向客户端发送服务端升级的消息
    		LOG.info("CometServer准备退出服务..........");
			Servlet cs = csHolder.getServlet();
			ServletContext context = cs.getServletConfig().getServletContext();
			//清理客户端连接
			CometConnectionManager ccm =(CometConnectionManager)context.getAttribute("cometserver.connect.manager");
			ccm.killAllConnection();
			
			//清理下发消息线程池
			StreamMsgPullFactory smpf =(StreamMsgPullFactory)context.getAttribute("cometserver.connect.factory");
			smpf.shutdown();
			//停止RPC线程组
    		//停止jetty服务
			synchronized(ccm){
				if(ccm.getConnectSize()>0){
					ccm.wait();
				}
			}
			ccm.clear();
			jetty.stop();
			LOG.info("CometServer成功退出..........");
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}