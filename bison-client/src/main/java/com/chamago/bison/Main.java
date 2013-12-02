package com.chamago.bison;

import java.io.File;

public class Main
{
  public Main(String config)
    throws Exception
  {
    new BisonContext(config);
    Thread.sleep(2000L);
    
  }

  public static void main(String[] args)
    throws Exception
  {
    if (System.getProperty("conf.dir") == null) {
      System.setProperty("conf.dir", "./conf");
    }
    String config = "";
    if ((args == null) || (args.length == 0))
      config = System.getProperty("conf.dir") + File.separator + "config.xml";
    else {
      config = args[0];
    }
    new Main(config);
  }
}