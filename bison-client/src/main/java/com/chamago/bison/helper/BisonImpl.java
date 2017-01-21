package com.chamago.bison.helper;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * @author Gavin.peng
 * 
 * 2013-10-27 下午04:08:54
 × bison-client
 */
public abstract class BisonImpl
  implements BisonObject
{
  public static final int DEFAULT_TIME_OUT = 10;
  protected long createTime;
  private String groupID = "1";
  protected int timeOut = 60;

  protected boolean ready = false;
  private static final long DEAD_LOCK_CHECK_INTERVAL = 5000L;
  private static final AtomicInteger MESSAGE_KEY = new AtomicInteger(0);
  private int key = MESSAGE_KEY.incrementAndGet();

  public long getCreateTime() {
    return this.createTime;
  }

  public String getGroupID() {
    return this.groupID;
  }

  public int getTimeOut() {
    return this.timeOut;
  }

  public void setGroupID(String groupID) {
    this.groupID = groupID;
  }

  public void setTimeOut(int timeOut) {
    this.timeOut = timeOut;
  }

  public int getKey() {
    return this.key;
  }

  protected boolean lockThread() {
    long endTime = System.currentTimeMillis() + this.timeOut * 1000L;
    if (endTime < 0L) {
      endTime = 9223372036854775807L;
    }

    synchronized (this) {
      if (this.ready) {
        return this.ready;
      }
      if (this.timeOut <= 0)
        return this.ready;
      do
      {
        try
        {
          long m_TimeOut = Math.min(this.timeOut * 1000L, 10000L);
          wait(m_TimeOut);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        if (this.ready)
          return true;
      }
      while (endTime >= System.currentTimeMillis());
      return this.ready;
    }
  }

  protected final void releaseThread()
  {
    synchronized (this) {
      try {
        this.ready = true;
        notifyAll();
      }
      catch (Exception localException)
      {
      }
    }
  }
}