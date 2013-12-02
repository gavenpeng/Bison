package com.chamago.bison.dbpool;

import com.chamago.bison.logger.Logger;
import com.chamago.bison.logger.LoggerFactory;
import com.chamago.bison.util.DateUtil;

import java.io.PrintStream;
import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

public class OracleJdbcConnect extends JdbcConnectImpl
{
  private Logger logger = LoggerFactory.getLogger("jdbc");

  public OracleJdbcConnect(String dataSource, Connection cn)
  {
    this.cn = cn;

    this.errCode = 0;
    this.callNum = 0L;

    this.lastSql = "";
    this.lastTime = "";
    this.errDesc = "";

    this.connectID = String.valueOf(System.currentTimeMillis());
  }

  public OracleJdbcConnect(Connection cn) {
    this.cn = cn;

    this.errCode = 0;
    this.callNum = 0L;

    this.lastSql = "";
    this.lastTime = "";
    this.errDesc = "";

    this.connectID = String.valueOf(System.currentTimeMillis());
  }

  public JdbcRecordSet executeQuery(String sql, int timeOut)
  {
    this.errCode = 0;
    this.errDesc = "";

    if ((sql == null) || (sql.length() >= 5000)) {
      this.logger.error("出错：语句过长 " + sql);
      this.errDesc = "SQL语句过长";
      this.errCode = -9999;
      return null;
    }

    JdbcRecordSet jRs = null;

    this.lastSql = sql;
    this.lastTime = DateUtil.getCurrentDateTime();
    incCallNum();
    Statement st = null;
    ResultSet rs = null;
    try {
      st = this.cn.createStatement(1004, 1008);
      st.setQueryTimeout(timeOut);
      rs = st.executeQuery(sql);
      jRs = new JdbcRecordSet(rs);
      rs.close();
      st.close();
    } catch (Exception e) {
      notifyCheck();
      this.logger.error("调试信息：" + sql, e);

      this.errDesc = e.getMessage();
      this.errCode = -1;
      try
      {
        if (rs != null) {
          rs.close();
          rs = null;
        }
      }
      catch (Exception localException1) {
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
    finally
    {
      try
      {
        if (rs != null) {
          rs.close();
          rs = null;
        }
      }
      catch (Exception localException3) {
      }
      try {
        if (st != null) {
          st.close();
          st = null;
        }
      } catch (Exception localException4) {
      }
    }
    return jRs;
  }

  public JdbcRecordSet executeQuery(String sql, Object[] ins, int timeout) {
    this.errCode = 0;
    this.errDesc = "";

    if ((sql == null) || (sql.length() >= 5000)) {
      this.logger.error("出错：语句过长 " + sql);
      this.errDesc = "SQL语句过长";
      this.errCode = -9999;
      return null;
    }

    JdbcRecordSet jrs = null;

    this.lastSql = sql;
    this.lastTime = DateUtil.getCurrentDateTime();
    incCallNum();
    PreparedStatement st = null;
    ResultSet rs = null;
    try
    {
      st = this.cn.prepareStatement(sql, 1004, 1008);
      st.setQueryTimeout(timeout);
      if ((ins != null) && (ins.length > 0)) {
        for (int i = 0; i < ins.length; i++) {
          st.setObject(i + 1, ins[i]);
        }
      }
      rs = st.executeQuery();
      jrs = new JdbcRecordSet(rs);
      rs.close();
      st.close();
    } catch (Exception e) {
      notifyCheck();

      String temp = "";
      if ((ins != null) && (ins.length > 0)) {
        for (int i = 0; i < ins.length; i++) {
          temp = temp + ins[i].toString() + ",";
        }
        if (temp.length() > 0) {
          temp = temp.substring(0, temp.length() - 1);
        }
      }
      this.logger.error("调试信息：" + sql + "\r\n\t 参数=" + temp, e);

      this.errDesc = e.getMessage();
      this.errCode = -1;
      try
      {
        if (rs != null) {
          rs.close();
          rs = null;
        }
        if (st != null) {
          st.close();
          st = null;
        }
      }
      catch (Exception localException1)
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
        if (st != null) {
          st.close();
          st = null;
        }
      }
      catch (Exception localException2) {
      }
    }
    return jrs;
  }

  public JdbcRecordSet executeQuery(String sql, int pageSize, int pageNo, int timeOut)
  {
    this.errCode = 0;
    this.errDesc = "";

    if ((sql == null) || (sql.length() >= 5000)) {
      this.logger.error("出错：语句过长 " + sql);
      this.errDesc = "SQL语句过长";
      this.errCode = -9999;
      return null;
    }

    JdbcRecordSet jRs = null;

    this.lastSql = sql;
    this.lastTime = DateUtil.getCurrentDateTime();
    incCallNum();
    Statement st = null;
    ResultSet rs = null;
    try
    {
      st = this.cn.createStatement(1004, 1007);
      st.setQueryTimeout(timeOut);

      String psql = "select * from (select rownum rec, a.* from (";
      psql = psql + sql + ") a where rownum <= " + pageSize * pageNo;
      psql = psql + ") where rec > " + pageSize * (pageNo - 1);

      rs = st.executeQuery(psql);
      jRs = new JdbcRecordSet(rs);

      rs.close();
      st.close();
    } catch (Exception e) {
      notifyCheck();
      this.logger.error("调试信息：" + sql, e);

      this.errDesc = e.getMessage();
      this.errCode = -1;
      try
      {
        if (rs != null) {
          rs.close();
          rs = null;
        }
      }
      catch (Exception localException1) {
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
    finally
    {
      try
      {
        if (rs != null) {
          rs.close();
          rs = null;
        }
      }
      catch (Exception localException3) {
      }
      try {
        if (st != null) {
          st.close();
          st = null;
        }
      } catch (Exception localException4) {
      }
    }
    return jRs;
  }

  public JdbcRecordSet executeQuery(String sql, Object[] ins, int pageSize, int pageNo, int timeOut)
  {
    this.errCode = 0;
    this.errDesc = "";

    if ((sql == null) || (sql.length() >= 5000)) {
      this.logger.error("出错：语句过长 " + sql);
      this.errDesc = "SQL语句过长";
      this.errCode = -9999;
      return null;
    }

    JdbcRecordSet jRs = null;

    this.lastSql = sql;
    this.lastTime = DateUtil.getCurrentDateTime();
    incCallNum();
    PreparedStatement st = null;
    ResultSet rs = null;
    try
    {
      String psql = "select * from (select rownum rec, a.* from (";
      psql = psql + sql + ") a where rownum <= " + pageSize * pageNo;
      psql = psql + ") where rec > " + pageSize * (pageNo - 1);

      st = this.cn.prepareStatement(psql, 1004, 1008);
      st.setQueryTimeout(timeOut);
      if ((ins != null) && (ins.length > 0)) {
        for (int i = 0; i < ins.length; i++) {
          st.setObject(i + 1, ins[i]);
        }
      }
      rs = st.executeQuery();
      jRs = new JdbcRecordSet(rs);
      rs.close();
      st.close();
    } catch (Exception e) {
      notifyCheck();

      String temp = "";
      if ((ins != null) && (ins.length > 0)) {
        for (int i = 0; i < ins.length; i++) {
          temp = temp + ins[i].toString() + ",";
        }
        if (temp.length() > 0) {
          temp = temp.substring(0, temp.length() - 1);
        }
      }
      this.logger.error("出错：" + sql + "\r\n\t 参数=" + temp, e);

      this.errDesc = e.getMessage();
      this.errCode = -1;
      try
      {
        if (rs != null) {
          rs.close();
          rs = null;
        }
      }
      catch (Exception localException1) {
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
    finally
    {
      try
      {
        if (rs != null) {
          rs.close();
          rs = null;
        }
      }
      catch (Exception localException3) {
      }
      try {
        if (st != null) {
          st.close();
          st = null;
        }
      } catch (Exception localException4) {
      }
    }
    return jRs;
  }

  public int executeUpdate(String sql, int timeout)
  {
    this.errCode = 0;
    this.errDesc = "";
    int ret = 0;

    if ((sql == null) || (sql.length() >= 5000)) {
      this.logger.error("调试信息：语句过长 " + sql);
      this.errDesc = "SQL语句过长";
      this.errCode = -9999;
      ret = -2;
      return ret;
    }

    this.lastSql = sql;
    this.lastTime = DateUtil.getCurrentDateTime();
    incCallNum();
    Statement st = null;
    try {
      st = this.cn.createStatement();
      st.setQueryTimeout(timeout);
      ret = st.executeUpdate(sql);
      st.close();
    } catch (Exception e) {
      notifyCheck();
      this.logger.error("出错：" + sql, e);
      this.errDesc = e.getMessage();
      this.errCode = -1;
      ret = -1;
      try
      {
        if (st != null) {
          st.close();
          st = null;
        }
      }
      catch (Exception localException1)
      {
      }
    }
    finally
    {
      try
      {
        if (st != null) {
          st.close();
          st = null;
        }
      }
      catch (Exception localException2) {
      }
    }
    return ret;
  }

  public int executeUpdate(String sql, Object[] ins, int timeout) {
    this.errCode = 0;
    this.errDesc = "";
    int ret = 0;

    if ((sql == null) || (sql.length() >= 5000)) {
      this.logger.error("调试信息：语句过长 " + sql);
      this.errDesc = "SQL语句过长";
      this.errCode = -9999;
      ret = -2;
      return ret;
    }

    this.lastSql = sql;
    this.lastTime = DateUtil.getCurrentDateTime();
    incCallNum();
    PreparedStatement st = null;
    try {
      st = this.cn.prepareStatement(sql);
      st.setQueryTimeout(timeout);

      if ((ins != null) && (ins.length > 0)) {
        for (int i = 0; i < ins.length; i++) {
          st.setObject(i + 1, ins[i]);
        }
      }
      ret = st.executeUpdate();
    } catch (Exception e) {
      notifyCheck();

      String temp = "";
      if ((ins != null) && (ins.length > 0)) {
        for (int i = 0; i < ins.length; i++) {
          temp = temp + ins[i].toString() + ",";
        }
        if (temp.length() > 0) {
          temp = temp.substring(0, temp.length() - 1);
        }
      }
      this.logger.error("出错：" + sql + "\r\n\t 参数=" + temp, e);

      this.errDesc = e.getMessage();
      this.errCode = -1;
      ret = -1;
      try
      {
        if (st != null) {
          st.close();
          st = null;
        }
      }
      catch (Exception localException1)
      {
      }
    }
    finally
    {
      try
      {
        if (st != null) {
          st.close();
          st = null;
        }
      }
      catch (Exception localException2) {
      }
    }
    return ret;
  }

  public int execProcedure(String spName, Object[] objInParam, Object[] objOutParam, int timeout)
  {
    this.errCode = 0;
    this.errDesc = "";

    incCallNum();
    CallableStatement pCall = null;
    int ret = 0;
    try {
      String strSql = "{call " + spName + "(";

      int count = objInParam.length + objOutParam.length;
      for (int i = 0; i < count; i++) {
        strSql = strSql + "?,";
      }
      strSql = strSql.substring(0, strSql.length() - 1);
      strSql = strSql + ")}";

      int num = 1;

      pCall = this.cn.prepareCall(strSql);
      if ((objInParam != null) && (objInParam.length > 0)) {
        for (int i = 0; i < objInParam.length; i++) {
          if ((objInParam[i] instanceof String)) {
            pCall.setString(num, (String)objInParam[i]);
          }
          else if ((objInParam[i] instanceof Long)) {
            pCall.setLong(num, ((Long)objInParam[i]).longValue());
          }
          else if ((objInParam[i] instanceof Integer)) {
            pCall.setInt(num, ((Integer)objInParam[i]).intValue());
          }
          else if ((objInParam[i] instanceof Double)) {
            pCall.setDouble(num, ((Double)objInParam[i]).doubleValue());
          }
          else if ((objInParam[i] instanceof Date)) {
            pCall.setTimestamp(num, new Timestamp(((Date)objInParam[i]).getTime()));
          }
          else if ((objInParam[i] instanceof Byte)) {
            pCall.setInt(num, ((Byte)objInParam[i]).byteValue());
          }
          else if ((objInParam[i] instanceof Short)) {
            pCall.setInt(num, ((Short)objInParam[i]).shortValue());
          }
          else if ((objInParam[i] instanceof Boolean)) {
            pCall.setBoolean(num, ((Boolean)objInParam[i]).booleanValue());
          }
          else if ((objInParam[i] instanceof Array)) {
            pCall.setArray(num, (Array)objInParam[i]);
          }
          else {
            System.out.println(objInParam[i].getClass().getName());
            throw new Exception("Unkown parameter type ");
          }
          num++;
        }
      }

      if ((objOutParam != null) && (objOutParam.length > 0)) {
        for (int i = 0; i < objOutParam.length; i++) {
          if ((objOutParam[i] instanceof String)) {
            pCall.setString(num, (String)objOutParam[i]);
            pCall.registerOutParameter(num, 12);
          }
          else if ((objOutParam[i] instanceof Long)) {
            pCall.setLong(num, ((Long)objOutParam[i]).longValue());
            pCall.registerOutParameter(num, 4);
          }
          else if ((objOutParam[i] instanceof Integer)) {
            pCall.setInt(num, ((Integer)objOutParam[i]).intValue());
            pCall.registerOutParameter(num, 4);
          }
          else if ((objOutParam[i] instanceof Double)) {
            pCall.setDouble(num, ((Double)objOutParam[i]).doubleValue());
            pCall.registerOutParameter(num, 8);
          }
          else if ((objOutParam[i] instanceof Date)) {
            pCall.setTimestamp(num, new Timestamp(((Date)objOutParam[i]).getTime()));
            pCall.registerOutParameter(num, 91);
          }
          else if ((objOutParam[i] instanceof Byte)) {
            pCall.setInt(num, ((Byte)objOutParam[i]).byteValue());
            pCall.registerOutParameter(num, 4);
          }
          else if ((objOutParam[i] instanceof Short)) {
            pCall.setInt(num, ((Short)objOutParam[i]).shortValue());
            pCall.registerOutParameter(num, 4);
          }
          else if ((objOutParam[i] instanceof Boolean)) {
            pCall.setBoolean(num, ((Boolean)objOutParam[i]).booleanValue());

            pCall.registerOutParameter(num, 16);
          }
          else if ((objOutParam[i] instanceof Array)) {
            pCall.setArray(num, (Array)objOutParam[i]);
            pCall.registerOutParameter(num, 2003, "T_PARAMS1");
          }
          else {
            throw new Exception("Unkown parameter registry");
          }
          num++;
        }
      }
      pCall.setQueryTimeout(timeout);
      pCall.execute();
      for (int i = 0; i < objOutParam.length; i++)
        objOutParam[i] = pCall.getObject(i + objInParam.length + 1);
    }
    catch (Exception e) {
      notifyCheck();
      this.errCode = -1;
      this.errDesc = e.getMessage();

      String temp = "";
      if ((objInParam != null) && (objInParam.length > 0)) {
        for (int i = 0; i < objInParam.length; i++) {
          temp = temp + objInParam[i].toString() + ",";
        }
        if (temp.length() > 0) {
          temp = temp.substring(0, temp.length() - 1);
        }
      }
      this.logger.error("调用存储过程 " + spName + " 异常 \r\n\t 参数=" + temp, e);
      ret = -1;
      try
      {
        if (pCall != null) {
          pCall.close();
          pCall = null;
        }
      }
      catch (Exception localException1)
      {
      }
    }
    finally
    {
      try
      {
        if (pCall != null) {
          pCall.close();
          pCall = null;
        }
      }
      catch (Exception localException2) {
      }
    }
    return ret;
  }

  public JdbcRecordSet execProcedure(String spName, Object[] objInParam, int timeout) {
    throw new RuntimeException("OracleJdbcConnect::execProcedure return resultset not implements");
  }
}