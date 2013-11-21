package com.chamago.bison.server;

import com.chamago.bison.codec.BeanCallCode;
import com.chamago.bison.codec.InterfaceCallInfo;
import com.chamago.bison.dbpool.JdbcPoolManager;
import com.chamago.bison.logger.Logger;
import com.chamago.bison.logger.LoggerFactory;
import com.chamago.bison.util.ByteUtil;
import com.chamago.bison.util.StringUtil;
import com.chamago.bison.util.ZipUtil;

import java.lang.reflect.Method;
import java.util.HashMap;
import org.apache.mina.core.session.IoSession;

/**
 * 
 * @author Gavin.peng
 * 
 * 2013-10-27 下午04:06:12
 × bison
 */
public class BisonBusiProcessor
{
  private static final boolean ZIP_FLAG = true;
  private final Logger logger = LoggerFactory.getLogger("bison");

  private ClassLoader myClassLoader = null;
  private HashMap<String, Object> caches = null;
  private HashMap<String, Method> m_caches = null;
  private HashMap<String, Method> m_interface = null;
  private HashMap<String, Object> c_interface = null;

  public BisonBusiProcessor()
  {
    this.m_caches = new HashMap<String, Method>();
    this.caches = new HashMap<String, Object>();
    this.m_interface = new HashMap<String, Method>();
    this.c_interface = new HashMap<String, Object>();
  }

  public void setClassLoader(ClassLoader clsLoader) {
    this.caches.clear();
    this.m_caches.clear();
    this.m_interface.clear();
    this.myClassLoader = clsLoader;
  }

  public void process_message(IoSession session, Object message, JdbcPoolManager pool, int tid) {
    byte[] msg = (byte[])message;

    int callType = ByteUtil.readInt(msg, 0);
    int skey = ByteUtil.readInt(msg, 4);
    if (callType == BeanCallCode.BEAN_CALL_ID)
      process_beancall(session, msg, pool, tid, skey); 
    else if (callType == BeanCallCode.INTERFACE_CALL_ID) {
      process_interfacecall(session, msg, pool, tid, skey);
    }
    msg = (byte[])null;
    message = null;
  }

  private void process_interfacecall(IoSession session, byte[] msg, JdbcPoolManager pool, int tid, int skey) {
    
	InterfaceCallInfo callInfo = (InterfaceCallInfo)ZipUtil.UnzipObject(msg, 8, msg.length, this.myClassLoader, ZIP_FLAG);
    int flag = callInfo.getCallFlag();
    int ret = 0;
    if (flag == 1) {
      String findClass = callInfo.getClassName() + "Stub";
      Object obj = this.c_interface.get(findClass);
      if (obj == null) {
        try {
          obj = this.myClassLoader.loadClass(findClass).newInstance();
          this.c_interface.put(findClass, obj);
        } catch (Exception e) {
          this.logger.error("接口加载 " + findClass + " 出现异常", e);
        }
      }

      if (obj == null)
        ret = -3;
    }
    else {
      String findClass = callInfo.getClassName() + "Stub";
      Object obj = this.c_interface.get(findClass);
      if (obj == null) {
        try {
          synchronized (this.c_interface) {
        	if(this.c_interface.get(findClass) == null){
              obj = this.myClassLoader.loadClass(findClass).newInstance();
              this.c_interface.put(findClass, obj);
        	}
          }
          obj = this.c_interface.get(findClass);
        } catch (Exception e) {
          this.logger.error("接口加载 " + findClass + " 出现异常", e);
        }
      }
      Object result = null;
      if (obj != null){
    	String imethodName = callInfo.getClassName() + "Stub"+"."+callInfo.getMethodName();
    	Method method = this.m_interface.get(imethodName);
    	if(method == null){
	        try {
	          synchronized (this.m_interface) {
	            if(this.m_interface.get(imethodName) == null){
	              method = obj.getClass().getMethod(callInfo.getMethodName(), callInfo.getParamTypes());
	              this.m_interface.put(imethodName, method);
	            }
	          }
	          method = this.m_interface.get(imethodName);
	        } catch (Exception e) {
	          e.printStackTrace();
	          method = null;
	        }
    	}
    	if(method !=null){
	    	try {
		      result = method.invoke(obj, callInfo.getParams());
	        } catch (Exception e) {
	          result = e;
	        }
    	}else{
    		ret = -4;
    	}
      }else {
        ret = -3;
      }
      callInfo.setResult(result);
    }

    byte[] b1 = ZipUtil.ZipObject(callInfo, true);
    byte[] buf = new byte[b1.length + 12];

    ByteUtil.writeInt(buf, 0, BeanCallCode.INTERFACE_CALL_ID);
    ByteUtil.writeInt(buf, 4, skey);
    ByteUtil.writeInt(buf, 8, ret);
    System.arraycopy(b1, 0, buf, 12, b1.length);

    session.write(buf);
    buf = (byte[])null;
    b1 = (byte[])null;
  }

  private void process_beancall(IoSession session, byte[] msg, JdbcPoolManager pool, int tid, int skey) {
    String methodNames = ByteUtil.readString(msg, 8);

    Object obj = ZipUtil.UnzipObject(msg, 8 + methodNames.length() + 1, msg.length, this.myClassLoader, ZIP_FLAG);
    String beanName = obj.getClass().getName();
    msg = (byte[])null;

    Object objStub = this.caches.get(beanName);
    if (objStub == null) {
      try {
        synchronized (this.caches) {
          if(this.caches.get(beanName) == null){
        	  objStub = this.myClassLoader.loadClass(beanName + "Stub").newInstance();
        	  this.caches.put(beanName, objStub);
          }
        }
        objStub = this.caches.get(beanName);
      } catch (Exception e1) {
        this.logger.error("BisonServerHandler::messageReceived ---> 类" + beanName + "Stub 不存在");
        objStub = null;
      }
    }

    int ret = 0;
    if (methodNames.length() == 0) {
      methodNames = "BeanCall";
    }
    String[] ss = StringUtil.splitter(methodNames, ",");
    long l = System.currentTimeMillis();
    if (objStub != null) {
      for (int i = 0; i < ss.length; i++) {
        String methodName = ss[i];
        Method objMethod = (Method)this.m_caches.get(beanName + "Stub." + methodName);
        if (objMethod == null) {
          try {
            synchronized (this.m_caches) {
              if(this.m_caches.get(beanName + "Stub." + methodName) == null){
            	  objMethod = objStub.getClass().getMethod(methodName, new Class[] { obj.getClass(), JdbcPoolManager.class, Integer.class });
            	  this.m_caches.put(beanName + "Stub." + methodName, objMethod);
              }
            }
            objMethod = (Method)this.m_caches.get(beanName + "Stub." + methodName);
          }
          catch (Exception e) {
            e.printStackTrace();
            objMethod = null;
          }
        }

        if (objMethod != null) {
          try {
            Object[] params = new Object[3];
            params[0] = obj;
            params[1] = pool;
            params[2] = Integer.valueOf(tid);

            objMethod.invoke(objStub, params);
          } catch (Exception e) {
            this.logger.error("BisonServerHandler::messageReceived ---> 方法" + beanName + "Stub." + methodName + " 出现异常");
            this.logger.error("", e);
            ret = -5;
            break;
          }
        } else {
          this.logger.error("BisonServerHandler::messageReceived ---> 方法" + beanName + "Stub." + methodName + " 不存在");
          ret = -4;
          break;
        }
      }
    } else {
      this.logger.error("BisonServerHandler::messageReceived ---> 类" + beanName + " 不存在");
      ret = -3;
    }
    l = System.currentTimeMillis() - l;
    if (l > 2000L) {
      this.logger.warn("Bean调用时间过长 BeanName=" + beanName + "." + methodNames + " " + l / 1000L);
    }

    byte[] b1 = ZipUtil.ZipObject(obj, true);
    byte[] buf = new byte[b1.length + 12];

    ByteUtil.writeInt(buf, 0, BeanCallCode.BEAN_CALL_ID);
    ByteUtil.writeInt(buf, 4, skey);
    ByteUtil.writeInt(buf, 8, ret);
    System.arraycopy(b1, 0, buf, 12, b1.length);

    session.write(buf);
    buf = (byte[])null;
    b1 = (byte[])null;
  }
}