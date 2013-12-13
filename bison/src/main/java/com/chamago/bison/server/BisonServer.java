package com.chamago.bison.server;

import com.chamago.bison.codec.BisonCodecFactory;
import com.chamago.bison.loader.JarClassLoader;
import com.chamago.bison.logger.Logger;
import com.chamago.bison.logger.LoggerFactory;
import com.chamago.bison.stream.BisonStreamServer;
import com.chamago.bison.util.xml.JXmlWapper;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Properties;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

/**
 * 
 * @author Gavin.peng
 * 
 * 2013-10-27 下午04:06:19
 × bison
 */
public class BisonServer
{
  private int maxQueueLength;
  private int maxQueueSize;
  private int DEFAULT_MAX_QUEUE_LENGTH=0;
  private int DEFAULT_MAX_QUEUE_SIZE=64 * 1024 * 1024;
  private Properties props;
  private int readBufferSize = 0;
  private int sendBufferSize = 0;
  private int receiveBufferSize = 0;
  private int port = 7100;
  private int handlers = 10;
  
  private final Logger logger;
  private BisonStreamServer streamServer;
  private BisonServerHandler minaHandler = null;
 
  public BisonServer() throws IOException {
	if (System.getProperty("conf.dir") == null) {
	   System.setProperty("conf.dir", "./conf");
	}
    this.logger = LoggerFactory.getLogger("bison");
    props = new Properties();
    initBison();
    offerService();
    createStreamServer();
    System.setProperty("SYSTEM_PROCESS_RECV_THREADS", String.valueOf(handlers));
    this.logger.info("Bison Server Listening on port " + port);
    this.logger.info("Bison Server config file {}/{}", System.getProperty("conf.dir"), "config.xml");
    
  }

  public static void main(String[] args)
    throws Exception
  {
	new BisonServer();
  }

  private void initBison(){
	  try {
	      JXmlWapper xml = JXmlWapper.parse(new File(System.getProperty("conf.dir") + File.separator + "config.xml"));
	      int count = xml.countXmlNodes("configuration.property");
	      for (int i = 0; i < count; i++){
	          JXmlWapper node = xml.getXmlNode("configuration.property[" + i + "]");
	          String name = node.getStringValue("@name");
	          String value = node.getStringValue("@value");
	          props.setProperty(name, value);
	      }
	      String maxQueueSize = props.getProperty("maxQueueSize");
	      this.maxQueueSize = maxQueueSize==null?DEFAULT_MAX_QUEUE_SIZE:Integer.parseInt(maxQueueSize);
	      String maxQueueLength = props.getProperty("maxQueueLength");
	      this.maxQueueLength = maxQueueLength==null?DEFAULT_MAX_QUEUE_LENGTH:Integer.parseInt(maxQueueLength);
	      String readBufferSize = props.getProperty("readBufferSize");
	      this.readBufferSize = readBufferSize==null?4096:Integer.parseInt(readBufferSize);
	      String sendBufferSize = props.getProperty("sendBufferSize");
	      this.sendBufferSize = sendBufferSize==null?4194304:Integer.parseInt(sendBufferSize);
	      String receiveBufferSize = props.getProperty("receiveBufferSize");
	      this.receiveBufferSize = receiveBufferSize==null?4194304:Integer.parseInt(receiveBufferSize);
	      String port = props.getProperty("port");
	      this.port = port==null?6100:Integer.parseInt(port);
	      String handlers = props.getProperty("handlers");
	      this.handlers = handlers==null?10:Integer.parseInt(handlers);
	      
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	  
  }
  
  
  protected void offerService() throws IOException{
	  
	  SocketAcceptor acceptor = new NioSocketAcceptor();
      acceptor.setReuseAddress(true);
      acceptor.getSessionConfig().setReadBufferSize(readBufferSize);
      acceptor.getSessionConfig().setSendBufferSize(sendBufferSize);
      acceptor.getSessionConfig().setReceiveBufferSize(receiveBufferSize);
      acceptor.getSessionConfig().setTcpNoDelay(false);
      acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 60);

      acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new BisonCodecFactory()));

      this.minaHandler = new BisonServerHandler(this);
      this.minaHandler.setClassLoader(new JarClassLoader());
      acceptor.setHandler(this.minaHandler);

      new ReloadBusiThread().start();
      
      acceptor.bind(new InetSocketAddress(port));
	  
  }
  
  public void createStreamServer() throws IOException{
	  this.streamServer = new BisonStreamServer();
  }
  
  public void stopService(){
	 this.minaHandler.cleanAll(); 
  }
    public int getMaxQueueLength() {
	 return maxQueueLength;
	}
	
	public void setMaxQueueLength(int maxQueueLength) {
		this.maxQueueLength = maxQueueLength;
	}
	
	public int getMaxQueueSize() {
		return maxQueueSize;
	}
	
	public void setMaxQueueSize(int maxQueueSize) {
		this.maxQueueSize = maxQueueSize;
	}
	
	public int getReadBufferSize() {
		return readBufferSize;
	}
	
	public void setReadBufferSize(int readBufferSize) {
		this.readBufferSize = readBufferSize;
	}
	
	public int getSendBufferSize() {
		return sendBufferSize;
	}
	
	public void setSendBufferSize(int sendBufferSize) {
		this.sendBufferSize = sendBufferSize;
	}
	
	public int getReceiveBufferSize() {
		return receiveBufferSize;
	}
	
	public void setReceiveBufferSize(int receiveBufferSize) {
		this.receiveBufferSize = receiveBufferSize;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public BisonServerHandler getHandler() {
		return this.minaHandler;
	}
	
	public void setHandler(BisonServerHandler handler) {
		this.minaHandler = handler;
	}


	
	public int getHandlers() {
		return handlers;
	}

	public void setHandlers(int handlers) {
		this.handlers = handlers;
	}



  class ReloadBusiThread extends Thread
  {
    HashMap<String, Long> hjar = new HashMap();

    ReloadBusiThread() {
    }

    public void run() {
      while (true){
        try {
          Thread.sleep(10000L);
          boolean blnNeed = false;
          String ss = System.getProperty("bison.service.home") + File.separator + "service";
          File dir = new File(ss);
          File[] files = dir.listFiles();
          for (int i = 0; i < files.length; ++i) {
	          String name = files[i].getName();
	          Long ll = (Long)this.hjar.get(name);
	          if ((ll != null) && (ll.longValue() != files[i].lastModified())) {
	            blnNeed = true;
	          }
	          this.hjar.put(name, new Long(files[i].lastModified()));
	
	          i++;
	          if (i < files.length)
	          {
	            continue;
	          }
          }
          if (blnNeed) {
            BisonServer.this.minaHandler.setClassLoader(new JarClassLoader()); continue;
          }
        }
        catch (Exception localException)
        {
        }
      }
    }
  }
  
}