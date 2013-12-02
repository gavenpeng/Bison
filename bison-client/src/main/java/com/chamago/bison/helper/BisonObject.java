package com.chamago.bison.helper;

/**
 * 
 * @author Gavin.peng
 * 
 * 2013-10-27 下午04:08:59
 × bison-client
 */
public abstract interface BisonObject
{
  public abstract long getCreateTime();

  public abstract void setTimeOut(int paramInt);

  public abstract int getTimeOut();

  public abstract boolean _onReceiveMessageEvent(int paramInt, Object paramObject);

  public abstract void _onTimeOut();

  public abstract void setGroupID(String paramString);

  public abstract String getGroupID();

  public abstract int getKey();

  public abstract Object getSendObject();

  public abstract boolean isAsync();

  public abstract String getMethodName();

  public abstract int getCallType();
}