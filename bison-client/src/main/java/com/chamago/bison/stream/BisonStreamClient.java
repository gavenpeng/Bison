/**
 * 
 */
package com.chamago.bison.stream;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.handler.stream.StreamIoHandler;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.chamago.bison.BisonClientHandler;
import com.chamago.bison.codec.BisonCodecFactory;
import com.chamago.bison.util.ByteUtil;

/**
 * @author Gavin.peng
 * 
 * 2013-10-17 下午12:02:06
 × bison-client
 */
public class BisonStreamClient {
	
	protected final SocketConnector connector;
	private static final int CONNECT_TIMEOUT = 30;
	private BisonStreamClientHandler streamHandler;
	public BisonStreamClient(){
		
		this.connector = new NioSocketConnector();
	    this.connector.getSessionConfig().setSendBufferSize(4194304);
	    this.connector.getSessionConfig().setReceiveBufferSize(4194304);
	    this.connector.getSessionConfig().setTcpNoDelay(true);
	    this.connector.getSessionConfig().setKeepAlive(true);
	    this.connector.setConnectTimeoutMillis(CONNECT_TIMEOUT);
	    this.streamHandler = new BisonStreamClientHandler();
	    this.connector.setHandler(this.streamHandler);
	    SocketAddress address = new InetSocketAddress("127.0.0.1", 7200);
	    ConnectFuture cf = this.connector.connect(address);
	    cf.awaitUninterruptibly();
	    if(cf.isConnected()){
	    	System.out.println("connection server 7200 is ok");
	    }else{
	    	System.out.println("connection server 7200 is failed");
	    }
	}
	
	public void readFile(String fileName) throws IOException{
		DataOutputStream out = (DataOutputStream) streamHandler.getOutStream();
		out.writeUTF(fileName);
		//ByteArrayOutputStream bout = new ByteArrayOutputStream();
		byte[] data = new byte[2048];
		int size = 0;
		int off = 0;
		DataInputStream in = (DataInputStream) streamHandler.getInputStream();
		int fileLength = in.readInt();
		byte[] content = new byte[fileLength];
		size = streamHandler.read(content);
		//bout.write(data,0,size);
		//byte[] content = bout.toByteArray();
		System.out.println("recevie file size is:"+size);
		FileOutputStream  fos = new FileOutputStream(new File("E://cmg-projects//bison-client//new_pom.xml"));
		fos.write(content);
		fos.flush();
		fos.close();
	}

	public static void main(String[] arg) throws IOException{
		BisonStreamClient bsc = new BisonStreamClient();
		try {
			Thread.sleep(2000);
			bsc.readFile("pom.xml");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
