package com.chamago.bison;

import com.chamago.bison.queue.Handler;

/**
 * 
 * @author Gavin.peng
 * 
 * 2013-10-27 下午04:06:19
 × bison
 */
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