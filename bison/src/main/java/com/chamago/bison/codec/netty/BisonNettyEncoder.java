package com.chamago.bison.codec.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by pengrongxin on 2017/1/15.
 */
public class BisonNettyEncoder extends MessageToByteEncoder {


    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {

        ByteBuf tmp = (ByteBuf)msg;

        int objectSize = tmp.writerIndex();
        if (objectSize > 4194304) {
            throw new IllegalArgumentException("The encoded object is too big: " + objectSize + " (> " + "4M" + ')');
        }

        System.out.println("writer msg to server:"+objectSize);

        out.writeInt(objectSize);
        out.writeBytes(tmp);

    }


}
