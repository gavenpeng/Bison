package com.chamago.bison;

import com.chamago.bison.queue.Handler;

public class ServiceContextUtil
{
  public static ServiceContext getServiceContext()
  {
    try
    {
      Handler dt = (Handler)Thread.currentThread();
      return (ServiceContext)dt.getAttachment();
    } catch (Exception e) {
      e.printStackTrace();
    }return null;
  }
}