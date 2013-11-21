package com.chamago.bison.thread;

import com.chamago.bison.dbpool.JdbcPoolManager;
import com.chamago.bison.util.xml.JXmlWapper;

/**
 * 
 * @author Gavin.peng
 * 
 * 2013-10-27 下午04:06:45
 × bison
 */
public abstract class BisonAbstractThread extends Thread
{
  private BisonThreadManager tManager;
  private long lastModified;
  private boolean threadFlag = false;
  private long threadInterval = 1000L;
  private String threadDesc;
  private String threadID;
  private int threadIdx;
  private JdbcPoolManager pool;

  public String getThreadID()
  {
    return this.threadID;
  }

  public void setThreadID(String threadID) {
    this.threadID = threadID;
  }

  public String getThreadDesc() {
    return this.threadDesc;
  }

  public void setThreadDesc(String threadDesc) {
    this.threadDesc = threadDesc;
  }

  public long getThreadInterval() {
    return this.threadInterval;
  }

  public void setThreadInterval(long threadInterval) {
    this.threadInterval = threadInterval;
  }

  public void startThread()
  {
    this.threadFlag = true;
    start();
  }

  public void stopThread() {
    this.threadFlag = false;
    interrupt();
  }

  

  
  public BisonThreadManager gettManager() {
	return tManager;
  }

  public void settManager(BisonThreadManager tManager) {
	this.tManager = tManager;
  }

  public long getLastModified() {
	return lastModified;
  }

  public void setLastModified(long lastModified) {
	this.lastModified = lastModified;
  }

public void run() {
    while (this.threadFlag) {
      try {
        thread_func();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      try
      {
        sleep(this.threadInterval);
      } catch (Exception localException1) {
      }
    }
  }

  public void thread_init(JXmlWapper elmConfig) {
  }

  public abstract void thread_func();

  public int getThreadIdx() {
    return this.threadIdx;
  }

  public void setThreadIdx(int threadIdx) {
    this.threadIdx = threadIdx;
  }

  public void setJdbcPoolManager(JdbcPoolManager pool) {
    this.pool = pool;
  }
  public JdbcPoolManager getJdbcPoolManager() {
    return this.pool;
  }
}