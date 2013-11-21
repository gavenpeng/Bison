package com.chamago.bison.logger;

import java.io.PrintWriter;

public class ConsoleLogger extends LoggerImpl
{
  private PrintWriter out;

  public ConsoleLogger(String name)
  {
    super(name);
    this.out = new PrintWriter(System.out);
  }

  protected void process(String o) {
    this.out.println(o);
    this.out.flush();
  }
}