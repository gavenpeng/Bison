package com.chamago.bison.codec;

import java.io.Serializable;

/**
 * 
 * @author Gavin.peng
 * 
 * 2013-10-27 下午04:08:34
 × bison-client
 */
public class InterfaceCallInfo
  implements Serializable
{
  private static final long serialVersionUID = -3499346075822912871L;
  private String className = null;
  private String methodName = null;
  private Class<?>[] paramTypes = null;
  private Object[] params = null;
  private Object result = null;
  private int callFlag;
  public static final int FIND = 1;
  public static final int CALL = 2;

  public int getCallFlag()
  {
    return this.callFlag;
  }

  public void setCallFlag(int callFlag) {
    this.callFlag = callFlag;
  }

  public Object getResult()
  {
    return this.result;
  }
  public void setResult(Object result) {
    this.result = result;
  }
  public String getClassName() {
    return this.className;
  }
  public void setClassName(String className) {
    this.className = className;
  }
  public String getMethodName() {
    return this.methodName;
  }
  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }
  public Class<?>[] getParamTypes() {
    return this.paramTypes;
  }
  public void setParamTypes(Class<?>[] paramTypes) {
    this.paramTypes = paramTypes;
  }
  public Object[] getParams() {
    return this.params;
  }
  public void setParams(Object[] params) {
    this.params = params;
  }
}