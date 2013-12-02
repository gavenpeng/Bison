package com.chamago.bison.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import com.chamago.bison.util.ByteUtil;

/**
 * 
 * @author Gavin.peng
 * 
 * 2013-10-27 下午04:08:23
 × bison-client
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
    in.get(tmp);
    if(len==12){
    int msgID = ByteUtil.readInt(tmp, 0);
    int key = ByteUtil.readInt(tmp, 4);
    int ret = ByteUtil.readInt(tmp, 8);
    System.out.println("msgID:"+msgID+",key:"+key+" ,ret:"+ret+"");
    }
    out.write(tmp);
    return true;
  }
}