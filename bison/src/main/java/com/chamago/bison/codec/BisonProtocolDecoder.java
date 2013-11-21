package com.chamago.bison.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/**
 * 
 * @author Gavin.peng
 * 
 * 2013-10-27 下午04:02:47
 × bison
 */
public class BisonProtocolDecoder extends CumulativeProtocolDecoder
{
  protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out)
    throws Exception
  {
    if (!in.prefixedDataAvailable(4, 4194304)) {
      return false;
    }
    int len = in.getInt();
    byte[] tmp = new byte[len];
    //把in缓冲区中的字节传输到tmp中。
    in.get(tmp);
    out.write(tmp);
    return true;
  }
}