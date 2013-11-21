package com.chamago.bison.dbpool;

import java.sql.Connection;

public abstract interface JdbcConnect
{
  public abstract JdbcConnectPool getParent();

  public abstract void setParent(JdbcConnectPool paramJdbcConnectPool);

  public abstract JdbcRecordSet executeQuery(String paramString);

  public abstract JdbcRecordSet executeQuery(String paramString, int paramInt);

  public abstract JdbcRecordSet executeQuery(String paramString, Object[] paramArrayOfObject);

  public abstract JdbcRecordSet executeQuery(String paramString, Object[] paramArrayOfObject, int paramInt);

  public abstract JdbcRecordSet executeQuery(String paramString, int paramInt1, int paramInt2);

  public abstract JdbcRecordSet executeQuery(String paramString, int paramInt1, int paramInt2, int paramInt3);

  public abstract JdbcRecordSet executeQuery(String paramString, Object[] paramArrayOfObject, int paramInt1, int paramInt2);

  public abstract JdbcRecordSet executeQuery(String paramString, Object[] paramArrayOfObject, int paramInt1, int paramInt2, int paramInt3);

  public abstract int executeUpdate(String paramString);

  public abstract int executeUpdate(String paramString, int paramInt);

  public abstract int executeUpdate(String paramString, Object[] paramArrayOfObject);

  public abstract int executeUpdate(String paramString, Object[] paramArrayOfObject, int paramInt);

  public abstract int execProcedure(String paramString, Object[] paramArrayOfObject1, Object[] paramArrayOfObject2);

  public abstract int execProcedure(String paramString, Object[] paramArrayOfObject1, Object[] paramArrayOfObject2, int paramInt);

  public abstract JdbcRecordSet execProcedure(String paramString, Object[] paramArrayOfObject);

  public abstract JdbcRecordSet execProcedure(String paramString, Object[] paramArrayOfObject, int paramInt);

  public abstract void lock();

  public abstract void unlock();

  public abstract boolean isLock();

  public abstract void incCallNum();

  public abstract String getLastSql();

  public abstract String getLastTime();

  public abstract String getErrDesc();

  public abstract int getErrCode();

  public abstract long getCallNum();

  public abstract Connection getConnection();

  public abstract String getServerType();

  public abstract void setServerType(String paramString);

  public abstract void setConnection(Connection paramConnection);

  public abstract long getLockTime();

  public abstract void closeConnect();

  public abstract String getConnectID();

  public abstract boolean beginTrans();

  public abstract boolean commit();

  public abstract boolean rollback();

  public abstract void notifyCheck();

  public abstract boolean isClosed(String paramString);

  public abstract int getRecordNums(String paramString);

  public abstract int getRecordNums(String paramString, Object[] paramArrayOfObject);
}