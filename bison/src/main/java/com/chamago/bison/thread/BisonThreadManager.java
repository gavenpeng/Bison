package com.chamago.bison.thread;

import com.chamago.bison.codec.netty.BisonServerNettyHandler;
import com.chamago.bison.dbpool.JdbcPoolManager;
import com.chamago.bison.loader.JarClassLoader;
import com.chamago.bison.logger.Logger;
import com.chamago.bison.logger.LoggerFactory;
import com.chamago.bison.server.BisonServerHandler;
import com.chamago.bison.util.xml.JXmlWapper;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

/**
 * 
 * @author Gavin.peng
 * 
 * 2013-10-15 下午12:11:01
 × bison
 */
public class BisonThreadManager
{
  private HashMap<String, BisonAbstractThread> hRbcBusiThreads;
  private int state = 0;
  protected BisonServerNettyHandler handler;
  private long configFileLastModified;
  protected final Logger logger = LoggerFactory.getLogger("bison");
  private String configFile;

  public BisonThreadManager(String cfgFile,BisonServerNettyHandler handler)
  {
	this.handler = handler;
    this.configFile = cfgFile;
    this.state = 0;
    this.hRbcBusiThreads = new HashMap<String, BisonAbstractThread>();
  }

  public void addBisonThread(BisonAbstractThread objThread) {
    this.hRbcBusiThreads.put(objThread.getThreadID(), objThread);
  }

  public void startBisonThread(String ids) {
    if (this.state != 0) {
      return;
    }
    
    File file = new File(this.configFile);
    this.configFileLastModified = file.lastModified();
    try {
      JXmlWapper wapper = JXmlWapper.parse(file);

      int count = wapper.countXmlNodes("threads.thread");
      for (int i = 0; i < count; i++)
        try {
          JXmlWapper xml = wapper.getXmlNode("threads.thread[" + i + "]");

          String id = xml.getStringValue("@id");
          String className = xml.getStringValue("@className");
          String desc = xml.getStringValue("@desc", "");
          long interval = xml.getLongValue("@interval", 1000L);

          if ((ids != null) && (!ids.equalsIgnoreCase(id))) continue;
          try {
            JarClassLoader myClassLoader = new JarClassLoader();
            BisonAbstractThread mct = (BisonAbstractThread)myClassLoader.loadClass(className).newInstance();
            mct.setContextClassLoader(myClassLoader);
            mct.setThreadID(id);
            mct.setThreadDesc(desc);
            mct.setName(desc);
            mct.setThreadInterval(interval);
            mct.setThreadIdx(i);
            //mct.setJdbcPoolManager(pool);
            mct.setLastModified(this.configFileLastModified);
            mct.settManager(this);
            mct.thread_init(xml.getXmlNode("config"));
            mct.startThread();
            addBisonThread(mct);

            this.logger.info("启动业务线程 " + desc);
          } catch (Exception e1) {
            this.logger.error("加载业务线程失败 className=" + className, e1);
          }
        }
        catch (Exception e) {
          this.logger.error("", e);
        }
    }
    catch (Exception e) {
      this.logger.error("", e);
    }
  }

  public void flushClientAccessips(String accessIps){

      //handler.setAccessIps(accessIps);
  }
  
  
  public void stopRbcThread(String ids) {
    try {
      Iterator iterator = this.hRbcBusiThreads.keySet().iterator();
      while (iterator.hasNext()) {
        Object key = iterator.next();
        BisonAbstractThread t = (BisonAbstractThread)this.hRbcBusiThreads.get(key);

        if ((ids == null) || (ids.equalsIgnoreCase(key.toString()))) {
          t.stopThread();
          this.logger.info("停止业务线程 " + t.getThreadDesc());
        }
      }
      if (ids == null)
        this.hRbcBusiThreads.clear();
      else
        this.hRbcBusiThreads.remove(ids);
    }
    catch (Exception localException)
    {
    }
  }
}