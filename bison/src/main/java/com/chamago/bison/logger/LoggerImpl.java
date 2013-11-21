package com.chamago.bison.logger;

import com.chamago.bison.logger.helper.FormattingTuple;
import com.chamago.bison.logger.helper.MessageFormatter;
import com.chamago.bison.queue.CallListener;
import com.chamago.bison.queue.CallQueueListener;
import com.chamago.bison.queue.LinkListQueue;
import com.chamago.bison.server.BisonServerHandler.Call;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class LoggerImpl
  implements Logger
{
  private static SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
  private static final String TRACE = "TRACE";
  private static final String DEBUG = "DEBUG";
  private static final String INFO = "INFO";
  private static final String WARN = "WARN";
  private static final String ERROR = "ERROR";
  private static final int I_TRACE = 0;
  private static final int I_DEBUG = 1;
  private static final int I_INFO = 2;
  private static final int I_WARN = 3;
  private static final int I_ERROR = 4;
  protected String logName;
  private int logLevel;
  private LinkListQueue<String> pQueue;

  public LoggerImpl(String name)
  {
    this.logName = name;
    this.pQueue = new LinkListQueue<String>();
    LogFlushHandler logHandler = new LogFlushHandler(new MyProcessImpl(),this.pQueue);
    logHandler.start();
  }

  public void setLogLevel(String level) {
    this.logLevel = 2;
    if ("TRACE".equalsIgnoreCase(level)) {
      this.logLevel = 0;
    }
    if ("DEBUG".equalsIgnoreCase(level)) {
      this.logLevel = 1;
    }
    if ("INFO".equalsIgnoreCase(level)) {
      this.logLevel = 2;
    }
    if ("WARN".equalsIgnoreCase(level)) {
      this.logLevel = 3;
    }
    if ("ERROR".equalsIgnoreCase(level))
      this.logLevel = 4;
  }

  public String getName()
  {
    return this.logName;
  }

  public boolean isDebugEnabled()
  {
    return this.logLevel <= 1;
  }

  public boolean isErrorEnabled()
  {
    return this.logLevel <= 4;
  }

  public boolean isInfoEnabled()
  {
    return this.logLevel <= 2;
  }

  public boolean isTraceEnabled()
  {
    return this.logLevel <= 0;
  }

  public boolean isWarnEnabled()
  {
    return this.logLevel <= 3;
  }

  public void trace(String msg)
  {
    if (isTraceEnabled())
      add_log_info(msg, "TRACE");
  }

  public void trace(String format, Object arg)
  {
    if (isTraceEnabled()) {
      FormattingTuple ft = MessageFormatter.format(format, arg);
      add_log_info(ft.getMessage(), ft.getThrowable(), "TRACE");
    }
  }

  public void trace(String format, Object arg1, Object arg2) {
    if (isTraceEnabled()) {
      FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
      add_log_info(ft.getMessage(), ft.getThrowable(), "TRACE");
    }
  }

  public void trace(String format, Object[] argArray) {
    if (isTraceEnabled()) {
      FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
      add_log_info(ft.getMessage(), ft.getThrowable(), "TRACE");
    }
  }

  public void trace(String msg, Throwable t) {
    if (isTraceEnabled())
      add_log_info(msg, t, "TRACE");
  }

  public void debug(String msg)
  {
    if (isDebugEnabled())
      add_log_info(msg, "DEBUG");
  }

  public void debug(String format, Object arg)
  {
    if (isDebugEnabled()) {
      FormattingTuple ft = MessageFormatter.format(format, arg);
      add_log_info(ft.getMessage(), ft.getThrowable(), "DEBUG");
    }
  }

  public void debug(String format, Object arg1, Object arg2) {
    if (isDebugEnabled()) {
      FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
      add_log_info(ft.getMessage(), ft.getThrowable(), "DEBUG");
    }
  }

  public void debug(String format, Object[] argArray) {
    if (isDebugEnabled()) {
      FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
      add_log_info(ft.getMessage(), ft.getThrowable(), "DEBUG");
    }
  }

  public void debug(String msg, Throwable t) {
    if (isDebugEnabled())
      add_log_info(msg, t, "DEBUG");
  }

  public void info(String msg)
  {
    if (isInfoEnabled())
      add_log_info(msg, "INFO");
  }

  public void info(String format, Object arg)
  {
    if (isInfoEnabled()) {
      FormattingTuple ft = MessageFormatter.format(format, arg);
      add_log_info(ft.getMessage(), ft.getThrowable(), "INFO");
    }
  }

  public void info(String format, Object arg1, Object arg2) {
    if (isInfoEnabled()) {
      FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
      add_log_info(ft.getMessage(), ft.getThrowable(), "INFO");
    }
  }

  public void info(String format, Object[] argArray) {
    if (isInfoEnabled()) {
      FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
      add_log_info(ft.getMessage(), ft.getThrowable(), "INFO");
    }
  }

  public void info(String msg, Throwable t) {
    if (isInfoEnabled())
      add_log_info(msg, t, "INFO");
  }

  public void warn(String msg)
  {
    if (isWarnEnabled())
      add_log_info(msg, "WARN");
  }

  public void warn(String format, Object arg)
  {
    if (isWarnEnabled()) {
      FormattingTuple ft = MessageFormatter.format(format, arg);
      add_log_info(ft.getMessage(), ft.getThrowable(), "WARN");
    }
  }

  public void warn(String format, Object arg1, Object arg2) {
    if (isWarnEnabled()) {
      FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
      add_log_info(ft.getMessage(), ft.getThrowable(), "WARN");
    }
  }

  public void warn(String format, Object[] argArray) {
    if (isWarnEnabled()) {
      FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
      add_log_info(ft.getMessage(), ft.getThrowable(), "WARN");
    }
  }

  public void warn(String msg, Throwable t) {
    if (isWarnEnabled())
      add_log_info(msg, t, "WARN");
  }

  public void error(String msg)
  {
    if (isErrorEnabled())
      add_log_info(msg, "ERROR");
  }

  public void error(String format, Object arg)
  {
    if (isErrorEnabled()) {
      FormattingTuple ft = MessageFormatter.format(format, arg);
      add_log_info(ft.getMessage(), ft.getThrowable(), "ERROR");
    }
  }

  public void error(String format, Object arg1, Object arg2) {
    if (isErrorEnabled()) {
      FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
      add_log_info(ft.getMessage(), ft.getThrowable(), "ERROR");
    }
  }

  public void error(String format, Object[] argArray) {
    if (isErrorEnabled()) {
      FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
      add_log_info(ft.getMessage(), ft.getThrowable(), "ERROR");
    }
  }

  public void error(String msg, Throwable t) {
    if (isErrorEnabled())
      add_log_info(msg, t, "ERROR");
  }

  private void add_log_info(String msg, String levles)
  {
    StringBuffer sb = new StringBuffer();
    sb.append("[").append(formater.format(new Date()));
    sb.append("] [").append(Thread.currentThread().getName());
    sb.append("] ").append(levles).append(" - ").append(msg);
    this.pQueue.offer(new String(sb));
  }

  private void add_log_info(String msg, Throwable t, String levles) {
    StringBuffer sb = new StringBuffer();
    sb.append("[").append(formater.format(new Date()));
    sb.append("] [").append(Thread.currentThread().getName());
    sb.append("] ").append(levles).append(" - ").append(msg);

    if (t != null) {
      StringWriter sw = new StringWriter();
      t.printStackTrace(new PrintWriter(sw));
      sb.append("\r\n").append(sw.toString());
    }

    this.pQueue.offer(new String(sb));
  }

  protected abstract void process(String paramString);

  class MyProcessImpl<E>
    implements CallQueueListener<E>
  {
    MyProcessImpl()
    {
    }

    public void processQueueElement(E o, int threadID)
    {
      LoggerImpl.this.process((String)o);
    }

  }
  
  class LogFlushHandler extends Thread{
	  private CallQueueListener listener;
	  private LinkListQueue<String> myQueue;
	  
	  public LogFlushHandler(CallQueueListener listener,LinkListQueue<String> pQueue){
		  this.listener = listener;
		  this.myQueue = pQueue;
	  }
	  
	  public void run(){
		  while(true){
			  try {
	    		 String obj = myQueue.take();
	    	     if (obj != null)
	    	        if (this.listener == null) {
	    	        	myQueue.offer(obj);
	    	          Thread.yield();
	    	        } else {
	    	          this.listener.processQueueElement(obj,1);
	    	        }
	    	    }
	    	    catch (Exception localException)
	    	    {
	    	    }  
		  }
	  }
  }
}