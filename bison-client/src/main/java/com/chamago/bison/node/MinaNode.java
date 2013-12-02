package com.chamago.bison.node;

import java.net.SocketAddress;
import org.apache.mina.core.session.IoSession;

/**
 * 
 * @author Gavin.peng
 * 
 * 2013-10-16 下午04:21:04
 × bison-client
 */
public class MinaNode
{
  private String nodeID;
  private String nodeIp;
  private int port;
  private String nodeName;
  private boolean connected;
  private IoSession session;
  private SocketAddress remoteAddress;

  public SocketAddress getRemoteAddress()
  {
    return this.remoteAddress;
  }
  public void setRemoteAddress(SocketAddress remoteAddress) {
    this.remoteAddress = remoteAddress;
  }
  public IoSession getSession() {
    return this.session;
  }
  public void setSession(IoSession session) {
    this.session = session;
  }

  public boolean isConnected() {
    return this.connected;
  }
  public void setConnected(boolean connected) {
    this.connected = connected;
  }

  public String getNodeName() {
    return this.nodeName;
  }
  public void setNodeName(String nodeName) {
    this.nodeName = nodeName;
  }
  public String getNodeID() {
    return this.nodeID;
  }
  public void setNodeID(String nodeID) {
    this.nodeID = nodeID;
  }
  public String getNodeIp() {
    return this.nodeIp;
  }
  public void setNodeIp(String nodeIp) {
    this.nodeIp = nodeIp;
  }
  public int getPort() {
    return this.port;
  }
  public void setPort(int port) {
    this.port = port;
  }

  public String toString() {
    return "nid=" + this.nodeID + ";nip=" + this.nodeIp + ";port=" + this.port;
  }
}