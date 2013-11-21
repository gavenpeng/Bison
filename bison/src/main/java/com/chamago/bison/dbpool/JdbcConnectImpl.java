package com.chamago.bison.dbpool;

import com.chamago.bison.logger.Logger;
import com.chamago.bison.logger.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.locks.ReentrantLock;

public abstract class JdbcConnectImpl
  implements JdbcConnect
{
  protected Logger logger = LoggerFactory.getLogger("jdbc");
  protected Connection cn;
  protected String lastSql;
  protected String lastTime;
  protected String errDesc;
  protected String connectID;
  protected int errCode;
  protected String serverType;
  protected long callNum;
  protected JdbcConnectPool parent;
  protected final ReentrantLock connLock = new ReentrantLock();
  private long lockTime;

  public void lock()
  {
    this.lockTime = System.currentTimeMillis();
    this.connLock.lock();
  }

  public void unlock()
  {
    this.connLock.unlock();
    this.lockTime = 9223372036854775807L;
  }

  public long getLockTime() {
    return this.lockTime;
  }

  public boolean isLock()
  {
    return this.connLock.isLocked();
  }

  public void incCallNum()
  {
    this.callNum += 1L;

    if ((this.callNum > this.parent.getMaxCallNum()) && (this.parent.getMaxCallNum() > 0)) {
      this.logger.info("数据库调用到最大次数，重新建立连接 ID=" + this.connectID);
      closeConnect();
      try {
        this.cn = this.parent.createRawConnect();
        this.callNum = 0L;
      } catch (Exception e) {
        this.logger.error("数据库调用到最大次数，重新建立连接异常 ID=" + this.connectID, e);
      }
    }
  }

  public String getLastSql()
  {
    return this.lastSql;
  }

  public String getLastTime()
  {
    return this.lastTime;
  }

  public String getErrDesc()
  {
    return this.errDesc;
  }

  public int getErrCode()
  {
    return this.errCode;
  }

  public long getCallNum()
  {
    return this.callNum;
  }

  public Connection getConnection()
  {
    incCallNum();
    return this.cn;
  }
  public void setConnection(Connection cn) {
    this.cn = cn;
  }

  public String getServerType()
  {
    return this.serverType;
  }

  public void setServerType(String serverType) {
    this.serverType = serverType;
  }

  public String getConnectID()
  {
    return this.connectID;
  }

  public void closeConnect() {
    try {
      this.cn.close();
    } catch (Exception localException) {
    }
    this.cn = null;
  }

  public boolean beginTrans() {
    try {
      if (this.cn.getAutoCommit()) {
        this.cn.setAutoCommit(false);
        return true;
      }
      return false;
    } catch (Exception e) {
    }
    return false;
  }

  public boolean commit()
  {
    try {
      if (this.cn.getAutoCommit()) {
        return false;
      }
      this.cn.commit();
      this.cn.setAutoCommit(true);
      return true;
    } catch (Exception e) {
    }
    return false;
  }

  public boolean rollback()
  {
    try {
      if (this.cn.getAutoCommit()) {
        return false;
      }
      this.cn.rollback();
      this.cn.setAutoCommit(true);
      return true;
    } catch (Exception e) {
    }
    return false;
  }

  public void notifyCheck()
  {
    if (this.parent != null)
      this.parent.notifyCheck();
  }

  public JdbcConnectPool getParent() {
    return this.parent;
  }

  public void setParent(JdbcConnectPool parent) {
    this.parent = parent;
  }

  public boolean isClosed(String sql)
  {
    lock();
    Statement st = null;
    ResultSet rs = null;
    boolean bln = false;
    try {
      st = this.cn.createStatement();
      rs = st.executeQuery(sql);
      rs.close();
      st.close();
      bln = false;
    } catch (SQLException sqle) {
      bln = true;
      try
      {
        if (rs != null) {
          rs.close();
          rs = null;
        }
      } catch (Exception localException1) {
      }
      try {
        if (st != null) {
          st.close();
          st = null;
        }
      }
      catch (Exception localException2)
      {
      }
    }
    catch (Exception e)
    {
      this.errDesc = e.getMessage();
      this.errCode = -1;
      bln = true;
      try
      {
        if (rs != null) {
          rs.close();
          rs = null;
        }
      } catch (Exception localException3) {
      }
      try {
        if (st != null) {
          st.close();
          st = null;
        }
      }
      catch (Exception localException4)
      {
      }
    }
    finally
    {
      try
      {
        if (rs != null) {
          rs.close();
          rs = null;
        }
      } catch (Exception localException5) {
      }
      try {
        if (st != null) {
          st.close();
          st = null;
        }
      } catch (Exception localException6) {
      }
    }
    unlock();
    return bln;
  }

  public JdbcRecordSet executeQuery(String sql) {
    return executeQuery(sql, 10);
  }

  public JdbcRecordSet executeQuery(String sql, Object[] ins) {
    return executeQuery(sql, ins, 10);
  }

  public JdbcRecordSet execProcedure(String spName, Object[] objInParam) {
    return execProcedure(spName, objInParam, 30);
  }

  public int execProcedure(String spName, Object[] objInParam, Object[] objOutParam) {
    return execProcedure(spName, objInParam, objOutParam, 30);
  }

  public int executeUpdate(String strSql) {
    return executeUpdate(strSql, 20);
  }

  public int executeUpdate(String strSql, Object[] ins) {
    return executeUpdate(strSql, ins, 20);
  }

  public JdbcRecordSet executeQuery(String sql, int pageSize, int pageNo) {
    return executeQuery(sql, pageSize, pageNo, 10);
  }

  public JdbcRecordSet executeQuery(String sql, Object[] ins, int pageSize, int pageNo) {
    return executeQuery(sql, ins, pageSize, pageNo, 10);
  }

  public int getRecordNums(String sql)
  {
    int ret = -1;
    try {
      JdbcRecordSet jrs = executeQuery(sql, new Object[0]);
      if ((jrs != null) && (jrs.size() > 0)) {
        jrs.first();
        ret = Integer.parseInt(jrs.getCurrentRecord()[0]);
      }
    } catch (Exception e) {
      ret = -1;
    }
    return ret;
  }

  public int getRecordNums(String sql, Object[] ins) {
    int ret = -1;
    try {
      JdbcRecordSet jrs = executeQuery(sql, ins);
      if ((jrs != null) && (jrs.size() > 0)) {
        jrs.first();
        ret = Integer.parseInt(jrs.getCurrentRecord()[0]);
      }
    } catch (Exception e) {
      ret = -1;
    }
    return ret;
  }
}