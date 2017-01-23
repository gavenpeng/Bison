package com.chamago.bison.codec.netty;

import com.chamago.bison.BisonContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.ByteBuffer;

/**
 * Created by pengrongxin on 2017/1/15.
 */
public class BisonClientNettyHandler extends ChannelInboundHandlerAdapter {

    private BisonContext bison;

    public BisonClientNettyHandler(BisonContext bison)
    {
        this.bison = bison;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //decode byteBuf

        System.out.println("receive server response.........");
        ByteBuf buf = (ByteBuf)msg;

        byte[] data = new byte[buf.writerIndex()];
        buf.readBytes(data);
        //ByteBuffer byteBuffer = ByteBuffer.allocate(buf.writerIndex());
        if (!this.bison.recvQueue.offer(data))
            System.out.println("接收消息入队列失败 ");
    }


}
