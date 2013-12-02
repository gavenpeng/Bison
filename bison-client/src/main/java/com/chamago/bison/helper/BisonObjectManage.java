package com.chamago.bison.helper;

import java.io.PrintStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 异步调用客户端句柄管理，在客户端线程调用服务端方法时，会把AsyncObject唯一标示key放到queue队列，
 * 在一打的时间没有返回时，对应的AsyncObject会进入
 * rqueue队列，通过TimeoutProcessor线程来清理掉。
 * @author Gavin.peng
 * 
 * 2013-10-8 下午06:09:12
 × bison-client
 */
public class BisonObjectManage
{
  private final Executor executor;
  private TimeoutProcessor processor;
  private LinkedBlockingQueue<Integer> queue;
  private LinkedBlockingQueue<Integer> rqueue;
  private ConcurrentHashMap<Integer, BisonObject>[] maps;
  private int m_TimeOut;
  private Integer curKey = null;

  public BisonObjectManage(Executor executor)
  {
    this.executor = executor;
    this.m_TimeOut = 10;
    this.queue = new LinkedBlockingQueue();
    this.rqueue = new LinkedBlockingQueue();

    this.maps = new ConcurrentHashMap[10];
    for (int i = 0; i < 10; i++)
      this.maps[i] = new ConcurrentHashMap();
  }

  public void addToTimeOutMonitor(int key)
  {
    this.queue.offer(Integer.valueOf(key));
    if (this.processor == null)
      startProcessor();
  }

  public void addObjectToManager(int key, BisonObject obj)
  {
    this.maps[(key % 10)].put(Integer.valueOf(key), obj);
  }

  public void removeManageObject(int key) {
    this.rqueue.offer(Integer.valueOf(key));
    if (this.processor == null)
      startProcessor();
  }

  public BisonObject findManageObject(int key)
  {
    return (BisonObject)this.maps[(key % 10)].get(Integer.valueOf(key));
  }

  private synchronized void startProcessor() {
    if (this.processor == null) {
      this.processor = new TimeoutProcessor();
      this.executor.execute(this.processor);
    }
  }

  private class TimeoutProcessor
    implements Runnable
  {
    private TimeoutProcessor()
    {
    }

    public void run()
    {
      do
      {
        while (true)
        {
          Integer rKey = (Integer)BisonObjectManage.this.rqueue.poll();
          
          if (rKey != null) {
        	  
            BisonObjectManage.this.maps[(rKey.intValue() % 10)].remove(rKey);

            continue;
          }

          if (BisonObjectManage.this.curKey == null) break;
          int idx = BisonObjectManage.this.curKey.intValue() % 10;
          BisonObject o = (BisonObject)BisonObjectManage.this.maps[idx].get(BisonObjectManage.this.curKey);
          if (o != null) {
            int iv = o.getTimeOut();
            if (iv == 0) {
              iv = BisonObjectManage.this.m_TimeOut;
            }
            if (iv > 60) {
              iv = 60;
            }

            long wtime = o.getCreateTime() + iv * 1000L - System.currentTimeMillis();
            if (wtime <= 0L) {
              BisonObjectManage.this.maps[idx].remove(BisonObjectManage.this.curKey);
              System.out.println("触发 Time out " + BisonObjectManage.this.curKey);
              o._onTimeOut();
              BisonObjectManage.this.curKey = null;
            } else {
              try {
                wait(wtime);
              } catch (Exception localException1) {
              }
            }
            o = null; continue;
          }
          BisonObjectManage.this.curKey = null;
        }
        try
        {
          BisonObjectManage.this.curKey = ((Integer)BisonObjectManage.this.queue.poll(1L, TimeUnit.MILLISECONDS));
        } catch (Exception e) {
          BisonObjectManage.this.curKey = null;
        }
      }
      while ((BisonObjectManage.this.curKey != null) || (BisonObjectManage.this.queue.size() != 0) || (BisonObjectManage.this.rqueue.size() != 0));
      BisonObjectManage.this.processor = null;
    }
  }
}