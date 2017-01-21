package com.chamago.bison.codec.netty;

import com.chamago.bison.ServiceContext;
import com.chamago.bison.core.Call;
import com.chamago.bison.dbpool.JdbcPoolManager;
import com.chamago.bison.logger.Logger;
import com.chamago.bison.logger.LoggerFactory;
import com.chamago.bison.queue.CallQueueListener;
import com.chamago.bison.queue.Handler;
import com.chamago.bison.queue.LinkListQueue;
import com.chamago.bison.server.BisonBusiProcessor;
import com.chamago.bison.server.BisonServer;
import com.chamago.bison.thread.BisonThreadManager;
import com.chamago.bison.util.xml.JXmlWapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledHeapByteBuf;
import io.netty.channel.*;
import org.cliffc.high_scale_lib.Counter;

import java.io.File;
import java.util.Hashtable;

/**
 * Created by pengrongxin on 2017/1/15.
 */
public class BisonServerNettyHandler extends SimpleChannelInboundHandler {

    private final Logger logger;
    private JdbcPoolManager pool = null;
    protected BisonServer bison;
    protected LinkListQueue<Call> recvQueue;
    private String accessIps;
    protected BisonBusiProcessor processor;
    protected Hashtable<String, Handler> hThreads;
    protected final Counter callQueueSize = new Counter();
    private BisonThreadManager rtm;


    public BisonServerNettyHandler(BisonServer bison){
        this.logger = LoggerFactory.getLogger("bison");
        this.bison = bison;

        //load_access_list();

        String cfgFile = System.getProperty("conf.dir") + File.separator + "config.xml";

        this.pool = new JdbcPoolManager(cfgFile);
        this.pool.loadDataSource();

        this.recvQueue = new LinkListQueue<Call>();
        this.processor = new BisonBusiProcessor();
        int handlers = bison.getHandlers();
        this.hThreads = new Hashtable<String, Handler>(handlers);
        CallQueueListener<Call> listener = new RecvListener<Call>();
        for (int i = 0; i < handlers; i++) {

            Handler handler = new Handler(recvQueue,i,"CallQueueConsumeThread");
            ServiceContext sc = new ServiceContext();
            sc.setJdbcPoolManager(this.pool);
            sc.setThreadID(i);
            handler.setAttachment(sc);
            handler.registerListener(listener);
            handler.setDaemon(true);
            handler.start();
            this.hThreads.put(String.valueOf(i), handler);
        }
        rtm = new BisonThreadManager(cfgFile,this);
        rtm.startBisonThread(null, this.pool);

        this.setClassLoader(bison.getClassLoader());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

        System.out.println("read mesg from client");
        ByteBuf buf = (ByteBuf)msg;
        int callSize = buf.writerIndex();
        if((callSize+callQueueSize.get())>bison.getMaxQueueSize()){
            logger.warn("bison server call queue size is too big,execced the max size:"+bison.getMaxQueueSize());
            //writerFull(message,session,null);
            return;
        }
        callQueueSize.add(callSize);

        byte[] data = new byte[buf.writerIndex()];
        buf.readBytes(data);
        if (!this.recvQueue.offer(new Call(ctx.channel(), data,callSize)))
            this.logger.error("入队列失败");

    }

    public void setClassLoader(ClassLoader clsLoader)
    {
        this.processor.setClassLoader(clsLoader);
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


    public BisonServer getBisonServer(){
        return this.bison;
    }


    class RecvListener<E>
            implements CallQueueListener<E>
    {
        RecvListener()
        {
        }

        public void processQueueElement(E o, int threadID)
        {
            Call obj = (Call)o;
            try {
                BisonServerNettyHandler.this.processor.process_message(obj.channel, obj.message, BisonServerNettyHandler.this.pool, threadID);
                callQueueSize.add(obj.size*-1);
            } catch (Exception e) {
                BisonServerNettyHandler.this.logger.error("", e);
            } finally {
                obj.channel = null;
                obj.message = null;
                obj.size = 0;
                obj = null;
            }
        }
    }



}
