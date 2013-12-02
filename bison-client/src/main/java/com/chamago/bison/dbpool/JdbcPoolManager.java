package com.chamago.bison.dbpool;

import com.chamago.bison.util.xml.JXmlWapper;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

public class JdbcPoolManager
{
  public static final String JDBC_POOL_NAME = "MY_JDBC_POOLS";
  private Hashtable<String, JdbcConnectPool> pools;
  public static final String JDBC_LOGGER_KEY = "jdbc";
  private String configFile;

  public JdbcPoolManager(String cfgFile)
  {
    this.configFile = cfgFile;
    this.pools = new Hashtable();
  }

  public void loadDataSource() {
    JXmlWapper xmlConfig = JXmlWapper.parse(new File(this.configFile));
    int count = xmlConfig.countXmlNodes("dataSource");
    for (int i = 0; i < count; i++) {
      JXmlWapper xmlds = xmlConfig.getXmlNode("dataSource[" + i + "]");
      String dataSource = xmlds.getStringValue("@name");

      String strDescName = xmlds.getStringValue("@desc");
      String driverName = xmlds.getStringValue("@driverName");
      String dbUrl = xmlds.getStringValue("@dbUrl");
      String dbUrlBak = xmlds.getStringValue("@dbUrlBak");
      String userName = xmlds.getStringValue("@userName");
      String passWord = xmlds.getStringValue("@passWord");
      String checkSql = xmlds.getStringValue("@checkSql");
      String serverType = xmlds.getStringValue("@serverType");
      int startNum = xmlds.getIntValue("@startNum", 1);
      int maxNum = xmlds.getIntValue("@maxNum", 5);
      int maxCallNum = xmlds.getIntValue("@maxCallNum", 0);
      int interval = xmlds.getIntValue("@interval", 60);

      JdbcConnectPool pool = new JdbcConnectPool();
      pool.setCheckSql(checkSql);
      pool.setDataSource(dataSource);
      pool.setDbUrl(dbUrl);
      pool.setDbUrlBak(dbUrlBak);
      pool.setDescName(strDescName);
      pool.setDriverName(driverName);
      pool.setInterval(interval);
      pool.setMaxCallNum(maxCallNum);
      pool.setMaxNum(maxNum);
      pool.setPassWord(passWord);
      pool.setServerType(serverType);
      pool.setStartNum(startNum);
      pool.setUserName(userName);
      try {
        pool.startCheckThread();
        pool.createConnects();
      }
      catch (Exception localException) {
      }
      this.pools.put(dataSource, pool);
    }
  }

  public void reload()
  {
  }

  public JdbcConnectPool findDataSource(String dataSource)
  {
    return (JdbcConnectPool)this.pools.get(dataSource.toLowerCase());
  }

  public Enumeration<String> getAllDataSourceName() {
    return this.pools.keys();
  }

  public synchronized JdbcConnect getJdbcConnect(String dataSource)
  {
    JdbcConnectPool pool = (JdbcConnectPool)this.pools.get(dataSource.toLowerCase());
    return pool.getJdbcConnect();
  }

  public JdbcConnect getJdbcConnect()
  {
    return getJdbcConnect("default");
  }

  public void addDataSource(JdbcConnectPool pool) {
    this.pools.put(pool.getDataSource(), pool);
  }

  public void removeDataSource(String dataSource) {
    JdbcConnectPool pool = findDataSource(dataSource);
    if (pool != null) {
      pool.stopCheckThread();
      pool.destory();
      this.pools.remove(dataSource);
      pool = null;
    }
  }

  public void destory() {
    if (this.pools != null) {
      Enumeration enum1 = this.pools.keys();
      while (enum1.hasMoreElements()) {
        String s = (String)enum1.nextElement();
        JdbcConnectPool pool = (JdbcConnectPool)this.pools.get(s);
        pool.stopCheckThread();
        pool.destory();
        this.pools.remove(s);
        pool = null;
      }
      this.pools.clear();
    }
  }
}