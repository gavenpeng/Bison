package com.chamago.bison.server;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.chamago.bison.codec.BeanCallCode;
import com.chamago.bison.codec.InterfaceCallInfo;
import com.chamago.bison.dbpool.JdbcPoolManager;
import com.chamago.bison.logger.Logger;
import com.chamago.bison.logger.LoggerFactory;
import com.chamago.bison.util.ByteUtil;
import com.chamago.bison.util.StringUtil;
import com.chamago.bison.util.ZipUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
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





  public void process_message(Channel session, Object message, JdbcPoolManager pool, int tid) throws IOException {



      byte[] msg = (byte[])message;

    int callType = ByteUtil.readInt(msg, 0);
    int skey = ByteUtil.readInt(msg, 4);
    if (callType == BeanCallCode.INTERFACE_CALL_ID) {
      process_interfacecall(session, msg, pool, tid, skey);
    }
    msg = (byte[])null;
    message = null;
  }

  private void process_interfacecall(Channel session, byte[] msg, JdbcPoolManager pool, int tid, int skey) throws IOException {

      ByteArrayInputStream in = new ByteArrayInputStream(msg,8,msg.length);
      Hessian2Input is = new Hessian2Input(in);
      InterfaceCallInfo callInfo = (InterfaceCallInfo)is.readObject();
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


      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      Hessian2Output out = new Hessian2Output(bos);
      out.writeObject(callInfo);
      byte[] data = bos.toByteArray();


      ByteBuf byteBuf = Unpooled.buffer(data.length + 12);
      byteBuf.writeInt(BeanCallCode.INTERFACE_CALL_ID);
      byteBuf.writeInt(skey);
      byteBuf.writeInt(ret);
      byteBuf.writeBytes(data);

      session.writeAndFlush(byteBuf);
    //session.write(buf);
      byteBuf = null;
      data = null;
  }


}