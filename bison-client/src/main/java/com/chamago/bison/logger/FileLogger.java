package com.chamago.bison.logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileLogger extends LoggerImpl
{
  private String logDir;
  private String lastDate;
  private SimpleDateFormat formater = new SimpleDateFormat("yyyyMMdd");
  private PrintWriter out;

  public String getLogDir()
  {
    return this.logDir;
  }

  public void setLogDir(String logDir) {
    this.logDir = logDir;

    File file = new File(logDir + File.separator + getName());
    if (!file.exists())
      file.mkdirs();
  }

  public FileLogger(String name)
  {
    super(name);
    this.lastDate = "";
  }

  protected void process(String o) {
    try {
      if (o != null) {
        String ss = this.formater.format(new Date());
        if ((ss.equalsIgnoreCase(this.lastDate)) && (this.out != null)) {
          this.out.println(o.toString());
          this.out.flush();
        } else {
          if (this.out != null) {
            this.out.close();
          }
          FileOutputStream fout = new FileOutputStream(new File(this.logDir + File.separator + getName(), ss + ".txt"), true);
          this.out = new PrintWriter(new PrintStream(fout));
          this.lastDate = ss;
          this.out.println(o.toString());
          this.out.flush();
        }
        o = null;
      }
    }
    catch (Exception localException)
    {
    }
  }
}