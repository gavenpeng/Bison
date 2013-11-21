/**
 * 
 */
package com.chamago.bison.stream;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.handler.stream.StreamIoHandler;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;


/**
 * @author Gavin.peng
 * 
 * 2013-10-17 上午10:58:24
 × bison
 */
public class BisonStreamServer {
	
	public static final int DEFAULT_DATA_SOCKET_SIZE = 128 * 1024;
	private static final int MIN_BUFFER_WITH_TRANSFERTO = 64*1024;
	private static final int port = 7200;
	private BisonStreamIoHandler ioHandler;
	
	public BisonStreamServer() throws IOException{
		
		createRemoteIOStream();
		
	}
	
	public void initStream(){
		
		
	}
	
	public void createRemoteIOStream() throws IOException{
		
	  SocketAcceptor acceptor = new NioSocketAcceptor();

      acceptor.setReuseAddress(true);
      acceptor.getSessionConfig().setSendBufferSize(MIN_BUFFER_WITH_TRANSFERTO);
      acceptor.getSessionConfig().setReceiveBufferSize(DEFAULT_DATA_SOCKET_SIZE);
      acceptor.getSessionConfig().setTcpNoDelay(true);
      acceptor.getSessionConfig().setKeepAlive(true);
      acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 60);
      
      this.ioHandler = new BisonStreamIoHandler();
      ioHandler.setReadTimeout(60);
      ioHandler.setWriteTimeout(60);
      acceptor.setHandler(ioHandler);
      acceptor.bind(new InetSocketAddress(port));
	}
	
	public void sendFile(File file){
		
	}
	
	

}
