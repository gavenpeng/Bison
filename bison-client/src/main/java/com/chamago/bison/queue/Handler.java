package com.chamago.bison.queue;

import com.chamago.bison.util.StringUtil;

/**
 * 
 * @author Gavin.peng
 * 
 * 2013-10-15 下午12:20:19
 × bison
 */
public class Handler<E> extends Thread
{
  private boolean blnFlag;
  private int threadID;
  private CallQueueListener<E> _listener;
  private Object attachment;
  private LinkListQueue<E> myQueue;
 
  public Handler(LinkListQueue<E> myQueue,int num,String desc){
	  this.myQueue = myQueue;
	  this.threadID = num;
	  this.blnFlag = true;
	  setName(desc + "-" + StringUtil.LeftPad(new StringBuilder(String.valueOf(num)).toString(), "0", 2));
  }
  
  public Object getAttachment()
  {
    return this.attachment;
  }

  public void setAttachment(Object attachment) {
    this.attachment = attachment;
  }


  public void run() {
    while (this.blnFlag){
    	 try {
    		 E obj = myQueue.take();
    	      if (obj != null)
    	        if (this._listener == null) {
    	        	myQueue.offer(obj);
    	          Thread.yield();
    	        } else {
    	          this._listener.processQueueElement(obj, threadID);
    	        }
    	    }
    	    catch (Exception localException)
    	    {
    	    }
    }
  }

  public void stopThread() {
    this.blnFlag = false;
    interrupt();
  }

  public void registerListener(CallQueueListener<E> listener) {
    this._listener = listener;
  }
}