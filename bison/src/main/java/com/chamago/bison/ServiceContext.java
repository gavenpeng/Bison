package com.chamago.bison;

import com.chamago.bison.dbpool.JdbcPoolManager;

/**
 * 
 * @author Gavin.peng
 * 
 * 2013-10-27 下午04:06:19
 × bison
 */
public class ServiceContext
{
  private JdbcPoolManager jdbcPoolManager;
  private int threadID;

  public JdbcPoolManager getJdbcPoolManager()
  {
    return this.jdbcPoolManager;
  }
  public void setJdbcPoolManager(JdbcPoolManager jdbcPoolManager) {
    this.jdbcPoolManager = jdbcPoolManager;
  }
  public int getThreadID() {
    return this.threadID;
  }
  public void setThreadID(int threadID) {
    this.threadID = threadID;
  }
}