/**
 * 
 */
package com.chamago.bison.stream;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.stream.StreamIoHandler;

import com.chamago.bison.util.ByteUtil;

/**
 * @author Gavin.peng
 * 
 * 2013-10-17 上午10:56:32
 × bison
 */
public class BisonStreamIoHandler extends StreamIoHandler {

	
	/* (non-Javadoc)
	 * @see org.apache.mina.handler.stream.StreamIoHandler#processStreamIo(org.apache.mina.core.session.IoSession, java.io.InputStream, java.io.OutputStream)
	 */
	@Override
	protected void processStreamIo(IoSession session, InputStream in,
			OutputStream out) {
		session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, 30);
		new DataXceiver(session,in,out).start();
	}
	
	
	@Override
    public void sessionIdle(IoSession session, IdleStatus status) {
		byte[] buf = new byte[8];
	    ByteUtil.write(buf, 0, 0);
	    ByteUtil.write(buf, 4, 1);
	    session.write(buf);
    }
	
	class DataXceiver extends Thread{
		
	  private IoSession streamSession;
	  private DataInputStream in;
	  private DataOutputStream out;
	  
	  public DataXceiver(IoSession session,InputStream in,OutputStream out){
		  this.streamSession = session;
		  this.in = new DataInputStream(in);
		  this.out = new DataOutputStream(out);
	  }
	  
	  public void run() {
		while(true){
		    try {
		    	//Thread.sleep(10000);
		    	//byte[] msg = new byte[1024];
		    	//int bytes = in.read(msg);
		    	//if(bytes>0){
			    	//System.out.println("request bytes size:"+bytes);
			    	String fileName = in.readUTF();
			    	if(fileName!=null){
				    	System.out.println("request read file:"+fileName);
				    	//this.streamSession.write();
				    	File sendFile = new File("E://cmg-projects//bison//"+fileName);
				    	this.out.writeLong(sendFile.length());
				    	
				    	streamSession.write(sendFile);
				    	//this.out.writeInt(0);//end
			    	}
		    	//}
//		    	FileInputStream fis = new FileInputStream(sendFile);
//		    	ByteArrayOutputStream bout = new ByteArrayOutputStream();
//				byte[] data = new byte[2048];
//				int size = 0;
//				int off = 0;
//				while((size=fis.read(data))>0){
//					bout.write(data,off,size);
//					off+=size;
//				}
//				byte[] content = bout.toByteArray();
//				System.out.println("file size is:"+content.length);
		    	//this.out.write(content);
		    	//msg = null;
		    } catch (Exception e) {
		    	e.printStackTrace();
		    
		    } finally {
		    	
		    }
		}
	  }
	}
}
