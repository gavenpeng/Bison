package com.chamago.bison.node;


import io.netty.channel.Channel;

import java.net.SocketAddress;

/**
 * Created by pengrongxin on 2017/1/15.
 */
public class BisonNode {

    private String nodeID;
    private String nodeIp;
    private int port;
    private String nodeName;
    private boolean connected;
    private Channel channel;
    private SocketAddress remoteAddress;

    public SocketAddress getRemoteAddress()
    {
        return this.remoteAddress;
    }
    public void setRemoteAddress(SocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
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
