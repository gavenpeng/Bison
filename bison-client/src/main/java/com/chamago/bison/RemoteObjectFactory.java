package com.chamago.bison;

import com.chamago.bison.codec.BeanCallCode;
import com.chamago.bison.codec.InterfaceCallInfo;
import com.chamago.bison.helper.BisonImpl;

import java.io.PrintStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 
 * @author Gavin.peng
 * 
 * 2013-10-27 下午04:08:03
 × bison-client
 */
public class RemoteObjectFactory
{
  private static ConcurrentHashMap<String, Object> maps = new ConcurrentHashMap();

  public static Object findRemoteObject(Class<?> clsType, String group, BisonContext context)
    throws RemoteException
  {
    Object obj = maps.get(clsType.getName() + "_" + group);
    if (obj == null) {
      if (context == null) {
        context = BisonContext.getDefaultContext();
      }

      InterfaceCallInfo callInfo = new InterfaceCallInfo();
      callInfo.setClassName(clsType.getName());
      callInfo.setMethodName("");

      ProxyCall call = new ProxyCall();
      call.setGroupID(group);
      //int rc = call.findRemoteInterface(callInfo, context);
      //if (rc == 0) {
        InvocationHandler handler = new MyInvocationHandler(context, group, clsType.getName());
        obj = Proxy.newProxyInstance(clsType.getClassLoader(), new Class[] { clsType }, handler);
        if (obj != null)
          maps.put(clsType.getName() + "_" + group, obj);
//      }
//      else
//      {
//        throw new RemoteException("find remote interface exception code=" + rc);
//      }
    }
    return obj;
  }

  public static Object findRemoteObject(Class<?> clsType, String group) throws RemoteException {
    return findRemoteObject(clsType, group, null);
  }

  static class MyInvocationHandler
    implements InvocationHandler
  {
    private BisonContext context;
    private String group;
    private String clsName;

    public MyInvocationHandler(BisonContext context, String group, String clsName)
    {
      this.context = context;
      this.group = group;
      this.clsName = clsName;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws RemoteException
    {
      InterfaceCallInfo callInfo = new InterfaceCallInfo();
      callInfo.setClassName(this.clsName);
      callInfo.setMethodName(method.getName());
      callInfo.setParamTypes(method.getParameterTypes());
      callInfo.setParams(args);

      RemoteObjectFactory.ProxyCall call = new RemoteObjectFactory.ProxyCall();
      call.setGroupID(this.group);

      int rc = call.callRemoteInterface(callInfo, this.context);
      if (rc == 0) {
        Object o = call.getResult();
        if ((o instanceof Exception)) {
          throw new RemoteException("call remote interface exception code=" + rc, (Exception)o);
        }
        return call.getResult();
      }

      throw new RemoteException("call remote interface exception code=" + rc);
    }
  }

  static class ProxyCall extends BisonImpl
  {
    private InterfaceCallInfo callInfo;
    private int errCode;
    private boolean async = false;
    private Object result;

    public boolean _onReceiveMessageEvent(int errCode, Object obj)
    {
      try
      {
        this.errCode = errCode;
        InterfaceCallInfo objCall = (InterfaceCallInfo)obj;
        if ((objCall != null) && 
          (objCall.getCallFlag() == 2)) {
          this.result = objCall.getResult();
        }

        objCall = null;
        obj = null;
        releaseThread();
      } catch (Exception e) {
        e.printStackTrace();
        this.errCode = -2;
        releaseThread();
      }
      return true;
    }

    public void _onTimeOut() {
      this.errCode = -1;
    }

    public int findRemoteInterface(InterfaceCallInfo call, BisonContext context) {
      this.ready = false;
      this.async = false;

      this.callInfo = call;
      this.callInfo.setCallFlag(1);
      this.errCode = 0;
      context.send_message(this);

      if (!lockThread()) {
        this.errCode = -1;
      }
      return this.errCode;
    }

    public int callRemoteInterface(InterfaceCallInfo callInfo) {
      return callRemoteInterface(callInfo, null);
    }

    protected int callRemoteInterface(InterfaceCallInfo callInfo, BisonContext context)
    {
      this.ready = false;
      this.async = false;
      this.callInfo = callInfo;
      this.callInfo.setCallFlag(2);

      this.errCode = 0;
      if (context == null) {
        context = BisonContext.getDefaultContext();
      }

      context.send_message(this);

      if (!lockThread()) {
        this.errCode = -1;
      }
      return this.errCode;
    }

    public int getErrCode() {
      return this.errCode;
    }

    public Object getResult() {
      return this.result;
    }

    public String getMethodName() {
      return "";
    }

    public Object getSendObject() {
      return this.callInfo;
    }

    public boolean isAsync() {
      return this.async;
    }

    public int getCallType() {
      return BeanCallCode.INTERFACE_CALL_ID;
    }
  }
}