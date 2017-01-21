package com.chamago.bison.core;

import io.netty.channel.Channel;

/**
 * Created by pengrongxin on 2017/1/15.
 */
public class Call {

    public Channel channel;
    public Object message;
    public int size;

    public Call(Channel channel, Object msg,int size)
    {
        this.channel = channel;
        this.message = msg;
        this.size = size;
    }
}
