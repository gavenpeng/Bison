package com.chamago.bison.codec.netty;

import com.chamago.bison.BisonContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * Created by pengrongxin on 2017/1/15.
 */
public class BisonChannelPipleFactory extends ChannelInitializer<SocketChannel> {
    private static final int MAX_FRAME_LENGTH = 4* 1024 * 1024;
    private static final int LENGTH_FIELD_OFFSET = 0;
    private static final int LENGTH_FIELD_LENGTH = 4;
    private static final int LENGTH_ADJUSTMENT = 0;
    private static final int INITIAL_BYTES_TO_STRIP = 4;

    private BisonContext bisonContext;

    public BisonChannelPipleFactory(BisonContext bisonContext){
        this.bisonContext = bisonContext;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast("frameDecoder",new BisonNettyDecoder(MAX_FRAME_LENGTH,LENGTH_FIELD_OFFSET,LENGTH_FIELD_LENGTH,LENGTH_ADJUSTMENT,INITIAL_BYTES_TO_STRIP,true));
        ch.pipeline().addLast("frameEncoder",new BisonNettyEncoder());
        ch.pipeline().addLast("userDecoder",new BisonClientNettyHandler(this.bisonContext));
    }
}
