package com.chamago.bison;

import com.chamago.bison.codec.BeanCallCode;
import com.chamago.bison.helper.BisonImpl;

import java.lang.reflect.Method;

/**
 * 
 * @author Gavin.peng
 * 
 * 2013-10-27 下午04:07:39
 × bison-client
 */
public class RemoteBeanCall<E> extends BisonImpl
{
  private int errCode;
  private E callBean;
  private boolean async;
  private String m_MethodName;
  private BisonContext rbcContext;
  private RemoteBeanCallHandler myHandler = null;

  public int RemoteCall(E obj, String method)
  {
    this.myHandler = null;
    int ret = send(obj, method, false);
    if (ret == 0) {
      if (!lockThread())
        _onTimeOut();
    }
    else {
      this.errCode = ret;
    }
    return this.errCode;
  }

  public int RemoteCall(E obj) {
    return RemoteCall(obj, "");
  }

  public void RemoteCall(E obj, RemoteBeanCallHandler objHandler)
  {
    RemoteCall(obj, objHandler, "");
  }

  public void RemoteCall(E obj, RemoteBeanCallHandler objHandler, String method) {
    this.myHandler = objHandler;
   
    this.errCode = send(obj, method, true);
    if ((this.errCode != 0) && (this.myHandler != null))
      this.myHandler.RemoteBeanCallEvent(this.errCode, this.callBean);
  }

  private int send(E obj, String method, boolean async)
  {
    this.ready = false;
    this.createTime = System.currentTimeMillis();
    this.callBean = obj;
    this.async = async;
    this.m_MethodName = method;
    return this.rbcContext.send_message(this);
  }

  public void _onTimeOut() {
    this.errCode = -1;
    this.rbcContext.removeManagedObject(getKey());
    if (this.myHandler != null)
      this.myHandler.RemoteBeanCallEvent(this.errCode, this.callBean);
  }

  public void setRbcContext(BisonContext rbcContext)
  {
    this.rbcContext = rbcContext;
  }

  public boolean _onReceiveMessageEvent(int errCode, Object obj) {
    try {
     
      this.errCode = errCode;
      if(obj != null){
    	  CopyBeanToBean(obj, this.callBean);
      }
      obj = null;
      releaseThread();
      if (this.myHandler != null)
        this.myHandler.RemoteBeanCallEvent(errCode, this.callBean);
    }
    catch (Exception e) {
      e.printStackTrace();
      this.errCode = -2;
      releaseThread();
      if (this.myHandler != null) {
        this.myHandler.RemoteBeanCallEvent(errCode, this.callBean);
      }
    }
    return true;
  }

  public int getErrCode() {
    return this.errCode;
  }

  public Object getSendObject() {
    return this.callBean;
  }

  public boolean isAsync() {
    return this.async;
  }

  public String getMethodName() {
    return this.m_MethodName;
  }

  public int getCallType() {
    return BeanCallCode.BEAN_CALL_ID;
  }

  private static void CopyBeanToBean(Object src, Object dest) throws Exception {
    Method[] method1 = src.getClass().getMethods();
    Method[] method2 = dest.getClass().getMethods();

    for (int i = 0; i < method1.length; i++) {
      String methodName1 = method1[i].getName();
      String methodFix1 = methodName1.substring(3, methodName1.length());

      if (methodName1.startsWith("get"))
        for (int j = 0; j < method2.length; j++) {
          String methodName2 = method2[j].getName();
          String methodFix2 = methodName2.substring(3, methodName2.length());
          if ((!methodName2.startsWith("set")) || 
            (!methodFix2.equals(methodFix1))) continue;
          Object[] objs1 = new Object[0];
          Object[] objs2 = new Object[1];
          objs2[0] = method1[i].invoke(src, objs1);
          method2[j].invoke(dest, objs2);
          objs1 = (Object[])null;
          objs2 = (Object[])null;
          break;
        }
    }
  }
}