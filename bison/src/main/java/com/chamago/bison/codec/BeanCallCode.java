package com.chamago.bison.codec;

/**
 * 
 * @author Gavin.peng
 * 
 * 2013-10-16 下午02:39:36
 × bison
 */
public class BeanCallCode
{
  public static final int CALL_SUCCESS = 0;
  public static final int CALL_FIND_SUCCESS = 1;
  public static final int CALL_TIMEOUT = -1;
  public static final int CALL_FAILURE = -2;
  public static final int CALL_NOTFOUND = -3;
  public static final int CALL_NOMETHOD = -4;
  public static final int CALL_EXCEPTION = -5;
  public static final int CALL_NOGROUP = -6;
  public static final int CALL_NOCONNECT = -7;
  public static final int CALL_RETURNNULL = -8;
  public static final int CALL_SEROVERLOAD = -9;
  public static final int BEAN_CALL_ID = 11;
  public static final int INTERFACE_CALL_ID = 22;

  public static final String getCodeDesc(int code)
  {
    String s = "";
    switch (code) {
    case 0:
      s = "成功";
      break;
    case -1:
      s = "调用超时";
      break;
    case -2:
      s = "调用失败";
      break;
    case -3:
      s = "服务类不存在";
      break;
    case -4:
      s = "调用方法不存在";
      break;
    case -5:
      s = "业务调用异常";
      break;
    case -6:
      s = "节点组无效";
      break;
    case -7:
      s = "未建立服务器连接";
      break;
    case -9:
      s = "服务端队列已满，可以修改maxQueueSize，增加队列大小";
      break;
    default:
      s = "其他错误";
    }

    return s;
  }
}