package com.chamago.bison;

import com.chamago.bison.codec.BeanCallCode;
import com.chamago.bison.codec.BisonCodecFactory;
import com.chamago.bison.dbpool.JdbcPoolManager;
import com.chamago.bison.helper.BisonObject;
import com.chamago.bison.helper.BisonObjectManage;
import com.chamago.bison.logger.Logger;
import com.chamago.bison.logger.LoggerFactory;
import com.chamago.bison.node.MinaNode;
import com.chamago.bison.node.NodeGroup;
import com.chamago.bison.queue.LinkListQueue;
import com.chamago.bison.queue.CallQueueListener;
import com.chamago.bison.queue.Handler;
import com.chamago.bison.stream.BisonStreamClient;
import com.chamago.bison.util.ByteUtil;
import com.chamago.bison.util.ZipUtil;
import com.chamago.bison.util.xml.JXmlWapper;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

/**
 * 
 * @author Gavin.peng
 * 
 * 2013-10-16 涓嬪崍02:46:50
 脳 bison-client
 */
public class BisonContext
{
  private static final int CONNECT_TIMEOUT = 30;
  private static final boolean ZIP_FLAG = true;
  private static final String SESSION_NODE_KEY = "bison.conetxt.sesionn.key";
  protected final Queue<MinaNode> connectQueue = new ConcurrentLinkedQueue();
  protected final ConcurrentHashMap<String, NodeGroup> groupMaps = new ConcurrentHashMap();
  protected final Executor executor;
  protected final Logger logger;
  protected final LinkListQueue<BisonObject> sendQueue;
  protected final LinkListQueue<Object> recvQueue;
  protected final SocketConnector connector;
  protected final BisonObjectManage[] amanager;
  protected boolean dispose = false;
  protected Processor processor;
  private String configFile;
  private JdbcPoolManager pool;
  private int handlers = 10;
  private static BisonContext defaultContext;
  private static BisonStreamClient streamClient;
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
    this.connector = new NioSocketConnector();

    this.connector.getSessionConfig().setSendBufferSize(4194304);
    this.connector.getSessionConfig().setReceiveBufferSize(4194304);
    this.connector.getSessionConfig().setTcpNoDelay(false);
    this.connector.getSessionConfig().setKeepAlive(true);
    this.connector.setConnectTimeoutMillis(CONNECT_TIMEOUT);
    this.connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new BisonCodecFactory()));


    this.connector.setHandler(new BisonClientHandler(this));
    this.amanager = new BisonObjectManage[10];
    Executor executor1 = Executors.newCachedThreadPool();
    for (int i = 0; i < 10; i++) {
      this.amanager[i] = new BisonObjectManage(executor1);
    }

    load_config();

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

      NodeGroup objGroup = new NodeGroup();
      objGroup.setGroupID(gid);
      objGroup.setGroupName(gName);

      int ncount = gnode.countXmlNodes("node");
      for (int j = 0; j < ncount; j++) {
        JXmlWapper node = gnode.getXmlNode("node[" + j + "]");
        String nid = node.getStringValue("@id");
        String nip = node.getStringValue("@ip");
        String name = node.getStringValue("@name");
        int port = Integer.parseInt(node.getStringValue("@port"));

        MinaNode objNode = new MinaNode();
        objNode.setNodeID(nid);
        objNode.setNodeIp(nip);
        objNode.setPort(port);
        objNode.setNodeName(name);

        SocketAddress address = new InetSocketAddress(nip, port);
        objNode.setRemoteAddress(address);

        this.connectQueue.offer(objNode);
        objGroup.addNode(objNode);
        this.logger.info("鍔犺浇鑺傜偣 --->  " + objNode.toString());
      }
      this.groupMaps.put(gid, objGroup);
    }
    startProcessor();
  }

  protected int send_message(BisonObject sender) {
    if (!this.sendQueue.offer(sender)) {
      System.out.println("鍙戦�娑堟伅 鍏ラ槦鍒楀け璐�);
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
          this.logger.error("娌℃湁鎵惧埌閫氱煡瀵硅薄 " + key);
        }
        this.amanager[idx].removeManageObject(key);
        o = null;
      }
      msg = (byte[])null;
      message = null;
    } catch (Exception e) {
      this.logger.error("澶勭悊娑堟伅鍑虹幇寮傚父", e);
    }
    message = null;
  }

  private int send_message_now(BisonObject obj) {
    int ret = 0;
    int idx = getManagerIdx(obj.getKey());
    NodeGroup objGroup = (NodeGroup)this.groupMaps.get(obj.getGroupID());
    if (objGroup != null) {
      MinaNode objNode = objGroup.getNode();
      if ((objNode != null) && (objNode.isConnected())) {
        this.amanager[idx].addObjectToManager(obj.getKey(), obj);
        if (obj.isAsync()) {
          this.amanager[idx].addToTimeOutMonitor(obj.getKey());
        }
        ret = sendObject(obj, objNode.getSession());
      } else {
    	if(objNode == null){
    		try {
    			System.out.println("绛夊緟鍜屾湇鍔＄寤虹珛杩炴帴 ");
				Thread.sleep(2000);
				send_message(obj);
				System.out.println("msg obj:閲嶆柊 杩涘叆鍙戦�闃熷垪");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}else{
    		
    	}
    	System.out.println("娌℃湁鍙互鐢ㄧ殑鎺ュ彈node");
        ret = -7;
      }
    } else {
      System.out.println("娌℃湁鍙互鐢ㄧ殑鎺ュ彈鏈嶅姟绔�);
      ret = -6;
    }
    return ret;
  }

  private int sendObject(BisonObject obj, IoSession session) {
    int ret = 0;
    try {
      int callType = obj.getCallType();
      if (callType == BeanCallCode.INTERFACE_CALL_ID) {
        byte[] data = ZipUtil.ZipObject(obj.getSendObject(), true);

        byte[] buf = new byte[8 + data.length];
        ByteUtil.writeInt(buf, 0, obj.getCallType());
        ByteUtil.writeInt(buf, 4, obj.getKey());
        System.arraycopy(data, 0, buf, 8, data.length);

        session.write(buf);
        data = (byte[])null;
        buf = (byte[])null;
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
      byte[] data = ZipUtil.ZipObject(message, true);

      byte[] buf = new byte[8 + data.length + methodName.length() + 1];
      ByteUtil.writeInt(buf, 0, 34952);
      ByteUtil.writeInt(buf, 4, 2147483647);
      ByteUtil.writeString(buf, 8, methodName);
      System.arraycopy(data, 0, buf, 8 + methodName.length() + 1, data.length);

      this.connector.broadcast(buf);
      data = (byte[])null;
      buf = (byte[])null;
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
    Map mapt = this.connector.getManagedSessions();
    Iterator iterator = mapt.keySet().iterator();
    while (iterator.hasNext()) {
      Long key = (Long)iterator.next();
      ((IoSession)mapt.get(key)).close(true);
    }
    try {
      Thread.sleep(3000L);
    }
    catch (Exception localException) {
    }
    this.connector.dispose();
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
      //System.out.println("鐢≧bcContext 鏉ュ彂閫�);
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
        MinaNode node = (MinaNode)BisonContext.this.connectQueue.poll();
        if ((node == null) && (BisonContext.this.connectQueue.size() == 0)) {
          BisonContext.this.processor = null;
          break;
        }
        ConnectFuture cf = BisonContext.this.connector.connect(node.getRemoteAddress());
        cf.awaitUninterruptibly();
        if (!cf.isConnected()) {
          BisonContext.this.logger.info("寤虹珛杩炴帴澶辫触 " + node.toString());
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
        cf.getSession().setAttribute(SESSION_NODE_KEY, node);
        node.setSession(cf.getSession());
        node.setConnected(true);
        BisonContext.this.logger.info("寤虹珛杩炴帴鎴愬姛 " + node.toString());
      }
    }
  }
}