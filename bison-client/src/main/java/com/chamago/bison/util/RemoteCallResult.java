package com.chamago.bison.util;

import java.io.Serializable;

public class RemoteCallResult
  implements Serializable
{
  private static final long serialVersionUID = -5497051939517775552L;
  private int errCode = -1;
  private String errDesc = "未使用";
  private String xml = "";
  private Object attach;

  public int getErrCode()
  {
    return this.errCode;
  }
  public void setErrCode(int errCode) {
    this.errCode = errCode;
  }
  public String getErrDesc() {
    return this.errDesc;
  }
  public void setErrDesc(String errDesc) {
    this.errDesc = errDesc;
  }
  public String getXml() {
    return this.xml;
  }
  public void setXml(String xml) {
    this.xml = xml;
  }
  public Object getAttach() {
    return this.attach;
  }
  public void setAttach(Object attach) {
    this.attach = attach;
  }

  public void setErrorInfo(int errCode, String errDesc) {
    this.errCode = errCode;
    this.errDesc = errDesc;
  }
}