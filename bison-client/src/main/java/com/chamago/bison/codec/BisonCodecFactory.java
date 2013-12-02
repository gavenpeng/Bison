package com.chamago.bison.codec;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * 
 * @author Gavin.peng
 * 
 * 2013-10-27 下午04:08:18
 × bison-client
 */
public class BisonCodecFactory
  implements ProtocolCodecFactory
{
  private final BisonProtocolEncoder encoder;
  private final BisonProtocolDecoder decoder;

  public BisonCodecFactory()
  {
    this(Thread.currentThread().getContextClassLoader());
  }

  public BisonCodecFactory(ClassLoader classLoader)
  {
    this.encoder = new BisonProtocolEncoder();
    this.decoder = new BisonProtocolDecoder();
  }

  public ProtocolDecoder getDecoder(IoSession arg0) throws Exception {
    return this.decoder;
  }

  public ProtocolEncoder getEncoder(IoSession arg0) throws Exception {
    return this.encoder;
  }
}