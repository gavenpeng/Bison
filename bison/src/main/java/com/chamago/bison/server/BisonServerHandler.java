package com.chamago.bison.server;

import com.chamago.bison.ServiceContext;
import com.chamago.bison.codec.BeanCallCode;
import com.chamago.bison.dbpool.JdbcPoolManager;
import com.chamago.bison.logger.Logger;
import com.chamago.bison.logger.LoggerFactory;
import com.chamago.bison.queue.CallQueueListener;
import com.chamago.bison.queue.Handler;
import com.chamago.bison.queue.LinkListQueue;
import com.chamago.bison.thread.BisonThreadManager;
import com.chamago.bison.util.ByteUtil;
import com.chamago.bison.util.xml.JXmlWapper;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.cliffc.high_scale_lib.Counter;

/**
 * 
 * @author Gavin.peng
 * 
 * 2013-10-15 下午12:18:11
 × bison
 */
public class BisonServerHandler extends IoHandlerAdapter
{
  
  private final Logger logger;
  private JdbcPoolManager pool = null;
  protected BisonServer bison;
  protected LinkListQueue<Call> recvQueue;
  private String accessIps;
  protected BisonBusiProcessor processor;
  protected Hashtable<String, Handler> hThreads;
  protected final Counter callQueueSize = new Counter();
  private  BisonThreadManager rtm;
  
  public BisonServerHandler(BisonServer bison)
  {
	  
    this.logger = LoggerFactory.getLogger("bison");
    this.bison = bison;
    
    load_access_list();
     
    String cfgFile = System.getProperty("conf.dir") + File.separator + "config.xml";
    
    this.pool = new JdbcPoolManager(cfgFile);
    this.pool.loadDataSource();

//    this.recvQueue = new LinkListQueue<Call>();
//    this.processor = new BisonBusiProcessor();
//    int handlers = bison.getHandlers();
//    this.hThreads = new Hashtable<String, Handler>(handlers);
//    CallQueueListener<Call> listener = new RecvListener<Call>();
//    for (int i = 0; i < handlers; i++) {
//
//      Handler handler = new Handler(recvQueue,i,"CallQueueConsumeThread");
//      ServiceContext sc = new ServiceContext();
//      sc.setJdbcPoolManager(this.pool);
//      sc.setThreadID(i);
//      handler.setAttachment(sc);
//      handler.registerListener(listener);
//      handler.setDaemon(true);
//      handler.start();
//      this.hThreads.put(String.valueOf(i), handler);
//    }
    //rtm = new BisonThreadManager(cfgFile,this);
    //rtm.startBisonThread(null, this.pool);
  }

  
  @Override
  public void sessionOpened(IoSession session) {
    session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, 30);

    if ((this.accessIps != null) && (this.accessIps.length() > 0)) {
      String ip = ((InetSocketAddress)session.getRemoteAddress()).getAddress().getHostAddress();
      //1 全量匹配，如果没有，则看是否有通配定义。
      if (this.accessIps.indexOf(ip) < 0) {
    	ip = ip.substring(0,ip.lastIndexOf("."));
    	if(this.accessIps.indexOf(ip)< 0){
    		this.logger.error("非法地址连接 ip=" + ip);
    	}
        session.close(true);
      }
    }
  }

  public void setAccessIps(String accessIps){
	  if(accessIps!=null){
		  this.accessIps = accessIps;
	  }
  }
  
  public BisonServer getBisonServer(){
	  return this.bison;
  }
  
  private void load_access_list() {
    try {
      File cFile = new File(System.getProperty("conf.dir") + File.separator + "config.xml");
      JXmlWapper xml = JXmlWapper.parse(cFile);
      int count = xml.countXmlNodes("access");
      this.accessIps = "";
      for (int i = 0; i < count; i++) {
        String ip = xml.getStringValue("access[" + i + "].@ip");
        if(ip!=null&&ip.endsWith(".*")){
        	ip = ip.substring(0, ip.indexOf(".*"));
        }
        this.accessIps = (this.accessIps + ip + ",");
        this.logger.info("授权 ip=" + ip);
      }
      cFile = null;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void messageReceived(IoSession session, Object message) throws Exception
  {
	byte[] buf = (byte[])message;
	int callSize = buf.length;
	if((callSize+callQueueSize.get())>bison.getMaxQueueSize()){
		logger.warn("bison server call queue size is too big,execced the max size:"+bison.getMaxQueueSize());
		writerFull(message,session,null);
		return;
	}
	callQueueSize.add(callSize);
    if (!this.recvQueue.offer(new Call(session, message,callSize)))
    	this.logger.error("入队列失败");
  }

  public void sessionIdle(IoSession session, IdleStatus status)
  {
    byte[] buf = new byte[8];
    ByteUtil.write(buf, 0, 0);
    ByteUtil.write(buf, 4, 1);
    session.write(buf);
  }
  
  public void writerFull(Object message,IoSession session, IdleStatus status)
  {
	byte[] msg = (byte[])message;
	int msgID = ByteUtil.readInt(msg, 0);
	int skey = ByteUtil.readInt(msg, 4);
    byte[] buf = new byte[12];
    ByteUtil.writeInt(buf, 0, msgID);
    ByteUtil.writeInt(buf, 4, skey);
    ByteUtil.writeInt(buf, 8, BeanCallCode.CALL_SEROVERLOAD);
    session.write(buf);
    buf = null;
  }

  public void cleanAll(){
	this.recvQueue.clear();
    Iterator<String> iterator = this.hThreads.keySet().iterator();
    while (iterator.hasNext()) {
      String key = iterator.next();
      Handler dt = (Handler)this.hThreads.get(key);
      dt.stopThread();
      this.logger.info("停止Bison RPC 线程 " + dt.getName());
      dt = null;
    }
    this.hThreads.clear();
    this.hThreads = null;  
	this.rtm.stopRbcThread(null);
  }
  
  public void exceptionCaught(IoSession session, Throwable cause) {
    session.close(false);
  }

  public void setClassLoader(ClassLoader clsLoader)
  {
	this.processor.setClassLoader(clsLoader);
  }

  class RecvListener<E>
    implements CallQueueListener<E>
  {
	RecvListener()
    {
    }

    public void processQueueElement(E o, int threadID)
    {
      BisonServerHandler.Call obj = (BisonServerHandler.Call)o;
//      try {
//        BisonServerHandler.this.processor.process_message(obj.session, obj.message, BisonServerHandler.this.pool, threadID);
//        callQueueSize.add(obj.size*-1);
//      } catch (Exception e) {
//        BisonServerHandler.this.logger.error("", e);
//      } finally {
//        obj.session = null;
//        obj.message = null;
//        obj.size = 0;
//        obj = null;
//      }
    }
  }

  public class Call
  {
    public IoSession session;
    public Object message;
    public int size;

    public Call(IoSession sess, Object msg,int size)
    {
      this.session = sess;
      this.message = msg;
      this.size = size;
    }
  }
}