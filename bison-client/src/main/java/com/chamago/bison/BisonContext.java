package com.chamago.bison;

import com.chamago.bison.codec.BeanCallCode;
import com.chamago.bison.codec.netty.BisonChannelPipleFactory;
import com.chamago.bison.codec.netty.BisonClientNettyHandler;
import com.chamago.bison.dbpool.JdbcPoolManager;
import com.chamago.bison.helper.BisonObject;
import com.chamago.bison.helper.BisonObjectManage;
import com.chamago.bison.logger.Logger;
import com.chamago.bison.logger.LoggerFactory;
import com.chamago.bison.node.BisonNode;
import com.chamago.bison.node.BisonGroup;
import com.chamago.bison.queue.LinkListQueue;
import com.chamago.bison.queue.CallQueueListener;
import com.chamago.bison.queue.Handler;
import com.chamago.bison.util.ByteUtil;
import com.chamago.bison.util.ZipUtil;
import com.chamago.bison.util.xml.JXmlWapper;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 
 * @author Gavin.peng
 * 
 * 2013-10-16 下午02:46:50
 × bison-client
 */
public class BisonContext
{
  private static final int CONNECT_TIMEOUT = 30;
  private static final boolean ZIP_FLAG = true;
  private static final String SESSION_NODE_KEY = "bison.conetxt.sesionn.key";
  protected final Queue<BisonNode> connectQueue = new ConcurrentLinkedQueue<BisonNode>();
  protected final ConcurrentHashMap<String, BisonGroup> groupMaps = new ConcurrentHashMap<String, BisonGroup>();
  protected  Executor executor;
  protected  Logger logger;
  public  LinkListQueue<BisonObject> sendQueue;
  public  LinkListQueue<Object> recvQueue;
  public  Bootstrap bootstrap;
  protected  BisonObjectManage[] amanager;
  protected boolean dispose = false;
  protected Processor processor;
  private String configFile;
  private JdbcPoolManager pool;
  private int handlers = 10;
  private static BisonContext defaultContext;
  protected Hashtable<String, Handler<BisonObject>> sThreads;
  protected Hashtable<String, Handler<Object>> rThreads;

  public BisonContext(String configFile)
  {
    this.configFile = configFile;

    this.pool = new JdbcPoolManager(configFile);
    this.pool.loadDataSource();

    this.logger = LoggerFactory.getLogger("bison");
    this.executor = Executors.newCachedThreadPool();

    this.sendQueue = new LinkListQueue<BisonObject>();
    this.recvQueue = new LinkListQueue<Object>();
    
    this.sThreads = new Hashtable<String, Handler<BisonObject>>(handlers);
    this.rThreads = new Hashtable<String, Handler<Object>>(handlers);
    
    CallQueueListener<Object> receProxy = new RecvProxy<Object>();
    CallQueueListener<BisonObject> sendProxy = new SendProxy<BisonObject>();
    
    for (int i = 0; i < handlers; i++) {
      Handler<BisonObject> shandler = new Handler<BisonObject>(sendQueue,i,"BisonSendThread");
      Handler<Object> rhandler = new Handler<Object>(recvQueue,i,"BisonRecvThread");
      shandler.registerListener(sendProxy);
      rhandler.registerListener(receProxy);
      shandler.setDaemon(true);
      rhandler.setDaemon(true);
      rhandler.start();
      shandler.start();
      this.sThreads.put(String.valueOf(i), shandler);
      this.rThreads.put(String.valueOf(i), rhandler);
    }

    this.amanager = new BisonObjectManage[10];
    Executor executor1 = Executors.newCachedThreadPool();
    for (int i = 0; i < 10; i++) {
      this.amanager[i] = new BisonObjectManage(executor1);
    }

    initNettyBootstrap();

    load_config();

  }


  public void initNettyBootstrap(){

      final BisonClientNettyHandler handler = new BisonClientNettyHandler(this);
      EventLoopGroup group = new NioEventLoopGroup();

      bootstrap = new Bootstrap();
      bootstrap.group(group)
              .channel(NioSocketChannel.class)
              .option(ChannelOption.TCP_NODELAY, true)
              .handler(new BisonChannelPipleFactory(this));
  }


  protected synchronized void startProcessor() {
    if ((this.processor == null) && (!this.dispose)) {
      this.processor = new Processor();
      this.executor.execute(this.processor);
    }
  }

  private void load_config() {
    JXmlWapper xml = JXmlWapper.parse(new File(this.configFile));
    int gcount = xml.countXmlNodes("group");
    for (int i = 0; i < gcount; i++) {
      JXmlWapper gnode = xml.getXmlNode("group[" + i + "]");
      String gid = gnode.getStringValue("@id");
      String gName = gnode.getStringValue("@name");

      BisonGroup objGroup = new BisonGroup();
      objGroup.setGroupID(gid);
      objGroup.setGroupName(gName);

      int ncount = gnode.countXmlNodes("node");
      for (int j = 0; j < ncount; j++) {
        JXmlWapper node = gnode.getXmlNode("node[" + j + "]");
        String nid = node.getStringValue("@id");
        String nip = node.getStringValue("@ip");
        String name = node.getStringValue("@name");
        int port = Integer.parseInt(node.getStringValue("@port"));

        BisonNode objNode = new BisonNode();
        objNode.setNodeID(nid);
        objNode.setNodeIp(nip);
        objNode.setPort(port);
        objNode.setNodeName(name);

        SocketAddress address = new InetSocketAddress(nip, port);
        objNode.setRemoteAddress(address);

        this.connectQueue.offer(objNode);
        objGroup.addNode(objNode);
        this.logger.info("加载节点 --->  " + objNode.toString());
      }
      this.groupMaps.put(gid, objGroup);
    }
    startProcessor();
  }

  protected int send_message(BisonObject sender) {
    if (!this.sendQueue.offer(sender)) {
      System.out.println("发送消息 入队列失败");
    }

    return 0;
  }

  private void process_recv_message(Object message) {
    try {
      byte[] msg = (byte[])message;
      int msgID = ByteUtil.readInt(msg, 0);
      if ((msgID == BeanCallCode.BEAN_CALL_ID) || (msgID == BeanCallCode.INTERFACE_CALL_ID)) {
    	int key = ByteUtil.readInt(msg, 4);
        int ret = ByteUtil.readInt(msg, 8);
        int idx = getManagerIdx(key);
        BisonObject o = this.amanager[idx].findManageObject(key);
        if (o != null) {
          Object obj = null;
          if(ret!=BeanCallCode.CALL_SEROVERLOAD){
        	  obj  = ZipUtil.UnzipObject(msg, 12, msg.length, null, true);
          }
          o._onReceiveMessageEvent(ret, obj);
          obj = null;
        } else {
          this.logger.error("没有找到通知对象 " + key);
        }
        this.amanager[idx].removeManageObject(key);
        o = null;
      }
      msg = (byte[])null;
      message = null;
    } catch (Exception e) {
      this.logger.error("处理消息出现异常", e);
    }
    message = null;
  }

  private int send_message_now(BisonObject obj) {
    int ret = 0;
    int idx = getManagerIdx(obj.getKey());
    BisonGroup objGroup = (BisonGroup)this.groupMaps.get(obj.getGroupID());
    if (objGroup != null) {
      BisonNode objNode = objGroup.getNode();
      if ((objNode != null) && (objNode.isConnected())) {
        this.amanager[idx].addObjectToManager(obj.getKey(), obj);
        if (obj.isAsync()) {
          this.amanager[idx].addToTimeOutMonitor(obj.getKey());
        }
        ret = sendObject(obj, objNode.getChannel());
      } else {
    	if(objNode == null){
    		try {
    			System.out.println("等待和服务端建立连接 ");
				Thread.sleep(2000);
				send_message(obj);
				System.out.println("msg obj:重新 进入发送队列");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}else{
    		
    	}
    	System.out.println("没有可以用的接受node");
        ret = -7;
      }
    } else {
      System.out.println("没有可以用的接受服务端");
      ret = -6;
    }
    return ret;
  }

  private int sendObject(BisonObject obj, Channel session) {
    int ret = 0;
    try {
      int callType = obj.getCallType();
      if (callType == BeanCallCode.INTERFACE_CALL_ID) {
        byte[] data = ZipUtil.ZipObject(obj.getSendObject(), true);

        ByteBuf byteBuf = Unpooled.buffer(8 + data.length);
        //byte[] buf = new byte[8 + data.length];
        byteBuf.writeInt(obj.getCallType());
        byteBuf.writeInt(obj.getKey());

        byteBuf.writeBytes(data);
//        ByteUtil.writeInt(buf, 0, obj.getCallType());
//        ByteUtil.writeInt(buf, 4, obj.getKey());
//        System.arraycopy(data, 0, buf, 8, data.length);
//        session.wr
        session.writeAndFlush(byteBuf);
        data = null;
        byteBuf = null;
        obj = null;
      } else {
        String methodName = obj.getMethodName();
        methodName = methodName == null ? "BeanCall" : methodName;

        byte[] data = ZipUtil.ZipObject(obj.getSendObject(), true);

        if (data == null) {
          this.logger.error("please check send object is seriable! " + obj.getSendObject().getClass().getName());
        }
        else {
          byte[] buf = new byte[8 + data.length + methodName.length() + 1];
          ByteUtil.writeInt(buf, 0, obj.getCallType());
          ByteUtil.writeInt(buf, 4, obj.getKey());
          ByteUtil.writeString(buf, 8, methodName);
          System.arraycopy(data, 0, buf, 8 + methodName.length() + 1, data.length);
          session.write(buf);
          data = (byte[])null;
          buf = (byte[])null;
          obj = null;
        }
      }
    } catch (Exception e) {
      this.logger.error("sendObject", e);
      e.printStackTrace();
      ret = -5;
    }
    return ret;
  }

  public void broadcast(Object message, String methodName)
  {
    try {
//      byte[] data = ZipUtil.ZipObject(message, true);
//
//      byte[] buf = new byte[8 + data.length + methodName.length() + 1];
//      ByteUtil.writeInt(buf, 0, 34952);
//      ByteUtil.writeInt(buf, 4, 2147483647);
//      ByteUtil.writeString(buf, 8, methodName);
//      System.arraycopy(data, 0, buf, 8 + methodName.length() + 1, data.length);

      //this.connector.broadcast(buf);
//      data = (byte[])null;
//      buf = (byte[])null;
      message = null;
    } catch (Exception e) {
      this.logger.error("broadcast", e);
      e.printStackTrace();
    }
  }

  protected void removeManagedObject(int key)
  {
    this.amanager[getManagerIdx(key)].removeManageObject(key);
  }

  private int getManagerIdx(int key)
  {
    return key % 100 / 10;
  }

  public void destory() {
    this.dispose = true;
    this.connectQueue.clear();
      //this.bootstrap.
//    Map mapt = this.connector.getManagedSessions();
//    Iterator iterator = mapt.keySet().iterator();
//    while (iterator.hasNext()) {
//      Long key = (Long)iterator.next();
//      ((IoSession)mapt.get(key)).close(true);
//    }
    try {
      Thread.sleep(3000L);
    }
    catch (Exception localException) {
    }
    this.bootstrap.group().shutdownGracefully();
    this.connectQueue.clear();
    this.groupMaps.clear();
    this.sendQueue.clear();
    this.recvQueue.clear();
    
    Iterator siterator = this.sThreads.keySet().iterator();
    while (siterator.hasNext()) {
      Object key = siterator.next();
      Handler dt = (Handler)this.sThreads.get(key);
      dt.stopThread();
      dt = null;
    }
    
    Iterator titerator = this.rThreads.keySet().iterator();
    while (titerator.hasNext()) {
      Object key = titerator.next();
      Handler dt = (Handler)this.rThreads.get(key);
      dt.stopThread();
      dt = null;
    }
  }

  public static void setDefaultContext(BisonContext context)
  {
    defaultContext = context;
  }
  public static BisonContext getDefaultContext() {
    return defaultContext;
  }


  private final class RecvProxy<E>
    implements CallQueueListener<E>
  {
    private RecvProxy()
    {
    }

    public void processQueueElement(E o, int threadID)
    {
      BisonContext.this.process_recv_message(o);
    }
  }

  private final class SendProxy<E>
    implements CallQueueListener<E>
  {
    private SendProxy()
    {
    }

    public void processQueueElement(E o, int threadID)
    {
      BisonContext.this.send_message_now((BisonObject)o);
    }

  }

  private final class Processor
    implements Runnable
  {
    private Processor()
    {
    }

    public void run()
    {
      while (true)
      {
        BisonNode node = (BisonNode)BisonContext.this.connectQueue.poll();
        if ((node == null) && (BisonContext.this.connectQueue.size() == 0)) {
          BisonContext.this.processor = null;
          break;
        }
        ChannelFuture cf = BisonContext.this.bootstrap.connect(node.getNodeIp(),node.getPort());
        //ConnectFuture cf = BisonContext.this.connector.connect(node.getRemoteAddress());
        cf.awaitUninterruptibly();
        if (!cf.isSuccess()) {
          BisonContext.this.logger.info("建立连接失败 " + node.toString());
          try {
            if (BisonContext.this.connectQueue.size() == 0)
              Thread.sleep(5000L);
            else
              Thread.sleep(1000L);
          }
          catch (Exception localException)
          {
          }
          BisonContext.this.connectQueue.offer(node); continue;
        }

        //cf.setAttribute(SESSION_NODE_KEY, node);
        node.setChannel(cf.channel());
        node.setConnected(true);
        BisonContext.this.logger.info("建立连接成功 " + node.toString());
      }
    }
  }
}