package com.chamago.bison.logger;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class LoggerFactory
{
  private static ConcurrentHashMap<String, Logger> maps = new ConcurrentHashMap();

  private static Properties prop = null;

  public static synchronized Logger getLogger(String name) {
    if (prop == null) {
      try {
        String confDir = System.getProperty("conf.dir");
        if ((confDir == null) || (confDir.length() == 0)) {
          confDir = System.getenv("conf.dir");
        }
        prop = new Properties();

        File file = new File(confDir, "log.properties");
        FileInputStream fis = new FileInputStream(file);

        prop.load(fis);

        fis.close();
        fis = null;
      } catch (Exception e) {
        e.printStackTrace();
        prop = null;
      }
    }

    Logger logger = (Logger)maps.get(name);
    if (logger == null) {
      String type = "CONSOLE";
      String level = "INFO";
      String dir = "";
      if (prop != null) {
        type = prop.getProperty(name + ".type");
        if (type == null) {
          type = prop.getProperty("root.type");
        }
        level = prop.getProperty(name + ".level");
        if (level == null) {
          level = prop.getProperty("root.level");
        }
        dir = prop.getProperty(name + ".dir");
        if (dir == null) {
          dir = prop.getProperty("root.dir");
        }
        if ((dir == null) || (dir.length() == 0)) {
          dir = ".." + File.separator + "logs";
        }
        if ((type == null) || (type.length() == 0)) {
          type = "CONSOLE";
        }
      }
      if (type.equalsIgnoreCase("FILE")) {
        FileLogger flogger = new FileLogger(name);
        flogger.setLogDir(dir);
        flogger.setLogLevel(level);

        logger = flogger;
      } else {
        ConsoleLogger clogger = new ConsoleLogger(name);
        clogger.setLogLevel(level);

        logger = clogger;
      }
      maps.put(name, logger);
    }
    return logger;
  }

  public static Logger getLogger(Class clz)
  {
    return getLogger(clz.getName());
  }
}