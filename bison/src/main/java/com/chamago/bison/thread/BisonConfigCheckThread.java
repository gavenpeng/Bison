/**
 * 
 */
package com.chamago.bison.thread;

import java.io.File;
import java.util.Properties;

import com.chamago.bison.server.BisonServer;
import com.chamago.bison.util.xml.JXmlWapper;

/**
 * @author Gavin.peng
 * 
 * 2013-10-15 下午12:07:45
 × bison
 */
public class BisonConfigCheckThread extends BisonAbstractThread {

	private Properties props;
	private int DEFAULT_MAX_QUEUE_SIZE=64 * 1024 * 1024;
	/* (non-Javadoc)
	 * @see com.mina.bison.thread.BisonAbstractThread#thread_func()
	 */
	@Override
	public void thread_func() {
		try {
		  File cFile = new File(System.getProperty("conf.dir") + File.separator + "config.xml");
		  if(cFile.lastModified()!=this.getLastModified()){
			  this.setLastModified(cFile.lastModified());
			  JXmlWapper xml = JXmlWapper.parse(cFile);
			  //更新access列表
		      int count = xml.countXmlNodes("access");
		      StringBuilder accessIps = new StringBuilder("");
		      for (int i = 0; i < count; i++) {
		        String ip = xml.getStringValue("access[" + i + "].@ip");
		        if(ip!=null&&ip.endsWith(".*")){
		        	ip = ip.substring(0, ip.indexOf(".*"));
		        }
		        accessIps.append(ip);
		        accessIps.append(",");
		      }
		      this.gettManager().flushClientAccessips(accessIps.toString());
		      this.gettManager().logger.info("更新服务端IP访问列表为："+accessIps.toString());
		      //
		      int propCount = xml.countXmlNodes("configuration.property");
		      for (int i = 0; i < propCount; i++){
		          JXmlWapper node = xml.getXmlNode("configuration.property[" + i + "]");
		          String name = node.getStringValue("@name");
		          String value = node.getStringValue("@value");
		          System.out.println("name:"+name+",value:"+value);
		          props.setProperty(name, value);
		      }
		      String maxQueueSize = props.getProperty("maxQueueSize");
		      int _maxQueueSize = maxQueueSize==null?DEFAULT_MAX_QUEUE_SIZE:Integer.parseInt(maxQueueSize);
		      String readBufferSize = props.getProperty("readBufferSize");
		      int _readBufferSize = readBufferSize==null?4096:Integer.parseInt(readBufferSize);
		      String sendBufferSize = props.getProperty("sendBufferSize");
		      int _sendBufferSize = sendBufferSize==null?4194304:Integer.parseInt(sendBufferSize);
		      String receiveBufferSize = props.getProperty("receiveBufferSize");
		      int _receiveBufferSize = receiveBufferSize==null?4194304:Integer.parseInt(receiveBufferSize);
		      
		      BisonServer bison = this.gettManager().handler.getBisonServer();
		      bison.setMaxQueueSize(_maxQueueSize);
		      bison.setReadBufferSize(_readBufferSize);
		      bison.setSendBufferSize(_sendBufferSize);
		      bison.setReceiveBufferSize(_receiveBufferSize);
		      
		  }
	    } catch (Exception e) {
	      e.printStackTrace();
	    }

	}

}
