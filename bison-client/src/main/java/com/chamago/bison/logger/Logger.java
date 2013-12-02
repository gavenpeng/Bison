package com.chamago.bison.logger;

public abstract interface Logger
{
  public static final String ROOT_LOGGER_NAME = "root";

  public abstract String getName();

  public abstract boolean isTraceEnabled();

  public abstract void trace(String paramString);

  public abstract void trace(String paramString, Object paramObject);

  public abstract void trace(String paramString, Object paramObject1, Object paramObject2);

  public abstract void trace(String paramString, Object[] paramArrayOfObject);

  public abstract void trace(String paramString, Throwable paramThrowable);

  public abstract boolean isDebugEnabled();

  public abstract void debug(String paramString);

  public abstract void debug(String paramString, Object paramObject);

  public abstract void debug(String paramString, Object paramObject1, Object paramObject2);

  public abstract void debug(String paramString, Object[] paramArrayOfObject);

  public abstract void debug(String paramString, Throwable paramThrowable);

  public abstract boolean isInfoEnabled();

  public abstract void info(String paramString);

  public abstract void info(String paramString, Object paramObject);

  public abstract void info(String paramString, Object paramObject1, Object paramObject2);

  public abstract void info(String paramString, Object[] paramArrayOfObject);

  public abstract void info(String paramString, Throwable paramThrowable);

  public abstract boolean isWarnEnabled();

  public abstract void warn(String paramString);

  public abstract void warn(String paramString, Object paramObject);

  public abstract void warn(String paramString, Object[] paramArrayOfObject);

  public abstract void warn(String paramString, Object paramObject1, Object paramObject2);

  public abstract void warn(String paramString, Throwable paramThrowable);

  public abstract boolean isErrorEnabled();

  public abstract void error(String paramString);

  public abstract void error(String paramString, Object paramObject);

  public abstract void error(String paramString, Object paramObject1, Object paramObject2);

  public abstract void error(String paramString, Object[] paramArrayOfObject);

  public abstract void error(String paramString, Throwable paramThrowable);
}