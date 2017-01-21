///**
// *
// */
//package com.chamago.bison.stream;
//
//import java.io.DataInputStream;
//import java.io.DataOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//
//import org.apache.mina.core.session.IdleStatus;
//import org.apache.mina.core.session.IoSession;
//import org.apache.mina.handler.stream.StreamIoHandler;
//
//import com.chamago.bison.util.ByteUtil;
//
///**
// * @author Gavin.peng
// *
// * 2013-10-17 下午02:25:40
// × bison-client
// */
//public class BisonStreamClientHandler extends StreamIoHandler {
//
//	private IoSession streamSession;
//	private DataInputStream in;
//	private DataOutputStream out;
//
//	/* (non-Javadoc)
//	 * @see org.apache.mina.handler.stream.StreamIoHandler#processStreamIo(org.apache.mina.core.session.IoSession, java.io.InputStream, java.io.OutputStream)
//	 */
//	@Override
//	protected void processStreamIo(IoSession session, InputStream in,
//			OutputStream out) {
//		this.streamSession = session;
//		this.in = new DataInputStream(in);
//		this.out = new DataOutputStream(out);
//		// TODO Auto-generated method stub
//		System.out.println("create stream connectiong with remote server");
//	}
//
//	@Override
//    public void sessionIdle(IoSession session, IdleStatus status) {
//		byte[] buf = new byte[8];
//	    ByteUtil.write(buf, 0, 0);
//	    ByteUtil.write(buf, 4, 1);
//	    session.write(buf);
//    }
//
//	public OutputStream getOutStream(){
//		return this.out;
//	}
//
//	public InputStream getInputStream(){
//		return this.in;
//	}
//
//	public int read(byte[] data) throws IOException{
//		return this.in.read(data);
//	}
//
//	public int read(byte[] data,int off,int len) throws IOException{
//		return this.in.read(data,off,len);
//	}
//
//	public void write(byte[] buf) throws IOException{
//		synchronized(out){
//			this.out.write(buf);
//		}
//	}
//
//	public void flush() throws IOException{
//		this.out.flush();
//	}
//}
