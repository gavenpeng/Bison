package com.chamago.bison.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/**
 * 
 * @author Gavin.peng
 * 
 * 2013-10-27 下午04:02:51
 × bison
 */
public class BisonProtocolEncoder extends ProtocolEncoderAdapter
{
  public void encode(IoSession session, Object message, ProtocolEncoderOutput out)
    throws Exception
  {
    IoBuffer buf = IoBuffer.allocate(64);
    buf.setAutoExpand(true);
    
    byte[] tmp = (byte[])message;
    
    buf.putInt(tmp.length);
    buf.put(tmp);

    int objectSize = buf.position() - 4;
    if (objectSize > 4194304) {
      throw new IllegalArgumentException("The encoded object is too big: " + objectSize + " (> " + "4M" + ')');
    }

    buf.flip();
    out.write(buf);
  }
}