package com.chamago.bison.dbpool;

import com.chamago.bison.logger.Logger;
import com.chamago.bison.logger.LoggerFactory;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Hashtable;

public class JdbcConnectPool
  implements Runnable
{
  private Hashtable<String, JdbcConnect> hConnectList;
  private String driverName;
  private String dbUrl;
  private String userName;
  private String passWord;
  private String dataSource;
  private int iStartNum;
  private int iMaxNum;
  private int iMaxCallNum;
  private String checkSql;
  private int iInterval;
  private String serverType;
  private String descName;
  private Thread me;
  private boolean blnRun;
  private String dbUrlBak;
  private String curDbUrl;
  private Connection cn;
  private Logger logger = LoggerFactory.getLogger("jdbc");

  public String getDbUrlBak() {
    return this.dbUrlBak;
  }

  public void setDbUrlBak(String dbUrlBak) {
    this.dbUrlBak = dbUrlBak;
  }

  public JdbcConnectPool() {
    this.hConnectList = new Hashtable();
  }

  protected void createConnects() throws SQLException, ClassNotFoundException, Exception {
    if ((this.curDbUrl == null) || (this.curDbUrl.length() == 0)) {
      this.curDbUrl = this.dbUrl;
    }
    for (int i = 0; i < this.iStartNum; i++) {
      JdbcConnect jcn = createConnect();
      if (this.hConnectList.size() < this.iMaxNum) {
        this.hConnectList.put(jcn.getConnectID(), jcn);
        jcn.setServerType(this.serverType);
        jcn.setParent(this);
      }
    }
  }

  protected void setDescName(String descName) {
    this.descName = descName;
  }

  protected void setDataSource(String dataSource) {
    this.dataSource = dataSource;
  }

  protected void setDriverName(String driverName) {
    this.driverName = driverName;
  }

  protected void setDbUrl(String dbUrl) {
    this.dbUrl = dbUrl;
  }

  protected void setUserName(String userName) {
    this.userName = userName;
  }

  protected void setPassWord(String passWord) {
    this.passWord = passWord;
  }

  protected void setStartNum(int iStartNum) {
    this.iStartNum = iStartNum;
  }

  protected int getStartNum() {
    return this.iStartNum;
  }

  protected void setMaxNum(int iMaxNum) {
    this.iMaxNum = iMaxNum;
  }

  protected int setMaxNum() {
    return this.iMaxNum;
  }

  protected void setCheckSql(String checkSql) {
    this.checkSql = checkSql;
  }

  protected String getCheckSql() {
    return this.checkSql;
  }

  protected void setInterval(int iInterval) {
    this.iInterval = iInterval;
  }

  protected int getInterval() {
    return this.iInterval;
  }

  protected void setMaxCallNum(int iMaxCallNum) {
    this.iMaxCallNum = iMaxCallNum;
  }

  protected int getMaxCallNum() {
    return this.iMaxCallNum;
  }

  public String getDescName() {
    return this.descName;
  }

  public String getDataSource() {
    return this.dataSource;
  }

  public String getDriverName() {
    return this.driverName;
  }

  public String getDbUrl() {
    return this.dbUrl;
  }

  public String getUserName() {
    return this.userName;
  }

  public String getPassWord() {
    return this.passWord;
  }

  public String getServerType() {
    return this.serverType;
  }

  public void setServerType(String serverType) {
    this.serverType = serverType;
  }

  public int getMaxNum() {
    return this.iMaxNum;
  }

  public synchronized JdbcConnect getJdbcConnect() {
    return getJdbcConnect(0);
  }

  public synchronized JdbcConnect getJdbcConnect(int num) {
    if (num > 500) {
      System.out.println("-----------------------------  10 秒没有取到连接 ----------------");

      return null;
    }
    JdbcConnect dcn = null;

    Enumeration enum1 = this.hConnectList.keys();
    while (enum1.hasMoreElements()) {
      String connectID = (String)enum1.nextElement();
      JdbcConnect tmp = (JdbcConnect)this.hConnectList.get(connectID);
      if (!tmp.isLock()) {
        if (dcn == null) {
          dcn = tmp;
        }
        else if (tmp.getCallNum() < dcn.getCallNum()) {
          dcn = tmp;
        }
      }

    }

    if ((dcn == null) && 
      (this.hConnectList.size() < this.iMaxNum)) {
      try {
        dcn = createConnect();
        dcn.setServerType(this.serverType);
        dcn.setParent(this);
        this.hConnectList.put(dcn.getConnectID(), dcn);
      } catch (Exception e) {
        e.printStackTrace();
        dcn = null;
      }

    }

    if (dcn == null) {
      if (!this.hConnectList.isEmpty())
        try {
          Thread.sleep(10L);
          dcn = getJdbcConnect(num++);
        } catch (Exception e) {
          e.printStackTrace();
          dcn = null;
        }
    }
    else {
      dcn.lock();
    }
    return dcn;
  }

  public JdbcConnect createConnect() throws SQLException, ClassNotFoundException {
    Connection cx = null;
    Class.forName(this.driverName);

    cx = DriverManager.getConnection(this.curDbUrl, this.userName, this.passWord);
    cx.setAutoCommit(true);

    JdbcConnect jcn = null;
    if (getServerType().equalsIgnoreCase("oracle"))
      jcn = new OracleJdbcConnect(this.dataSource, cx);
    else if (getServerType().equalsIgnoreCase("mssql")){
      //jcn = new MssqlJdbcConnect(this.dataSource, cx);
    }else if (getServerType().equalsIgnoreCase("mysql")) {
      jcn = new OracleJdbcConnect(this.dataSource, cx);
    }
    jcn.setServerType(this.serverType);

    this.logger.info("建立数据库连接成功！" + this.dataSource);
    this.logger.info(this.curDbUrl);
    return jcn;
  }

  protected Connection createRawConnect() throws SQLException, ClassNotFoundException {
    Connection cx = null;
    Class.forName(this.driverName);

    cx = DriverManager.getConnection(this.curDbUrl, this.userName, this.passWord);
    cx.setAutoCommit(true);

    this.logger.info("建立数据库连接成功！" + this.dataSource);
    this.logger.info(this.curDbUrl);
    return cx;
  }

  public int size()
  {
    return this.hConnectList.size();
  }

  public void startCheckThread()
  {
    if (this.me == null) {
      this.blnRun = true;
      this.me = new Thread(this);
      this.me.setName("JDBC");
      this.me.setDaemon(true);
      this.me.start();
    }
  }

  public void stopCheckThread()
  {
    if (this.me != null) {
      this.blnRun = false;
      this.me.interrupt();
      this.me = null;
    }
    try
    {
      if (this.cn != null) {
        this.cn.close();
        this.cn = null;
      }
    }
    catch (Exception localException)
    {
    }
  }

  public boolean isRun()
  {
    return this.blnRun;
  }

  public synchronized void destory() {
    Enumeration enum1 = this.hConnectList.keys();
    while (enum1.hasMoreElements()) {
      String connectID = (String)enum1.nextElement();
      JdbcConnect jcn = (JdbcConnect)this.hConnectList.get(connectID);

      jcn.closeConnect();
      this.hConnectList.remove(connectID);
      jcn = null;
    }
    stopCheckThread();
  }

  public void run()
  {
    if (this.iInterval == 0) {
      this.iInterval = 1;
    }
    this.logger.info("启动连接检查线程 " + this.iInterval);
    while (this.blnRun)
      try {
        if (checkDataBaseLive())
          Thread.sleep(60000 * this.iInterval);
        else
          Thread.sleep(1000L);
      }
      catch (Exception localException)
      {
      }
    this.logger.info("连接检查线程退出 " + this.iInterval);
  }

  private boolean checkDataBaseLive() {
    boolean ret = false;
    if ((this.checkSql == null) || (this.checkSql.length() == 0)) {
      return true;
    }

    if ((this.curDbUrl == null) || (this.curDbUrl.length() == 0)) {
      this.curDbUrl = this.dbUrl;
    }
    try
    {
      if (this.cn == null) {
        Class.forName(this.driverName);
        this.cn = DriverManager.getConnection(this.curDbUrl, this.userName, this.passWord);
        this.cn.setAutoCommit(true);
      }
    } catch (Exception e) {
      e.printStackTrace();
      this.cn = null;

      if ((this.dbUrlBak != null) && (this.dbUrlBak.length() > 0)) {
        if (this.curDbUrl.equalsIgnoreCase(this.dbUrl))
          this.curDbUrl = this.dbUrlBak;
        else {
          this.curDbUrl = this.dbUrl;
        }
        this.logger.info("数据库连接切换 ds=" + this.dataSource + " dbUrl=" + this.curDbUrl);
      }
    }

    if (this.cn != null) {
      Statement st = null;
      ResultSet rs = null;
      try {
        st = this.cn.createStatement();
        rs = st.executeQuery(this.checkSql);
      } catch (Exception e) {
        this.logger.error("数据库连接检查异常 checkSql=" + this.checkSql, e);
        try {
          this.cn.close();
        } catch (Exception localException1) {
        }
        this.cn = null;
        try
        {
          if (rs != null) {
            rs.close();
            rs = null;
          }
        }
        catch (Exception localException2) {
        }
        try {
          if (st != null) {
            st.close();
            st = null;
          }
        }
        catch (Exception localException3)
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
        catch (Exception localException4) {
        }
        try {
          if (st != null) {
            st.close();
            st = null;
          }
        }
        catch (Exception localException5) {
        }
      }
    }
    if (this.cn == null) {
      try {
        Enumeration enum1 = this.hConnectList.keys();
        while (enum1.hasMoreElements()) {
          Object key = enum1.nextElement();
          JdbcConnect jcn = (JdbcConnect)this.hConnectList.get(key);
          this.logger.info("删除连接 ds=" + this.dataSource + " ID=" + jcn.getConnectID());
          jcn.closeConnect();
          jcn = null;
        }
      } catch (Exception localException8) {
      }
      this.hConnectList.clear();
    } else {
      try {
        if (this.hConnectList.isEmpty())
          createConnects();
        else
          checkConnectState();
      }
      catch (Exception localException9) {
      }
      ret = true;
    }
    return ret;
  }

  private void checkConnectState()
  {
    try
    {
      Enumeration enum1 = this.hConnectList.keys();
      while (enum1.hasMoreElements()) {
        String connectID = (String)enum1.nextElement();
        JdbcConnect jcn = (JdbcConnect)this.hConnectList.get(connectID);
        boolean blnRemove = false;
        if ((jcn.isLock()) && (jcn.getLockTime() < System.currentTimeMillis() - 300000L)) {
          jcn.unlock();
        }
        else if ((this.checkSql.length() > 0) && 
          (jcn.isClosed(this.checkSql))) {
          blnRemove = true;
        }

        if (blnRemove) {
          this.hConnectList.remove(connectID);
          jcn.closeConnect();
          jcn = null;
          this.logger.info("删除连接 " + this.dataSource + " " + connectID);
        }
      }
    } catch (Exception localException) {
    }
  }

  public void notifyCheck() {
    this.me.interrupt();
  }
}