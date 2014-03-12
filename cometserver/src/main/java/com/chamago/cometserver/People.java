/**
 * 
 */
package com.chamago.cometserver;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.continuation.Continuation;


public class People {

	public static LinkListQueue<String> queue = new LinkListQueue<String>();
	
	private static String ENTER_CHARS = "\r\n";
	
	private static int msgNum = 1;
	
	private Continuation continuation;
	
	public volatile boolean supend = false;
	
	public PullThread pt;
	public TimeMessage tm;
	
	static{
		
	}
	
	public People(){
		init();
	}
	
	public void init(){
		tm = new TimeMessage("created-thread");
		tm.start();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int t = Runtime.getRuntime().availableProcessors();
		System.out.println((16*3)/4);
		People p = new People();
//		try {
//			Thread.sleep(10000);
//		} catch (InterruptedException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		p.tm.interrupt();
		try {
			long st = System.currentTimeMillis();
			System.out.println("等待线程结束:time"+st);
			p.tm.join();
			long et = System.currentTimeMillis();
			System.out.println("线程已经结束:time is :"+(et-st));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	public boolean hasEvent(){
		return queue.size()>0?true:false;
	}

	public Continuation getContinuation() {
		return continuation;
	}

	public void setContinuation(Continuation continuation) {
		this.continuation = continuation;
	}

	
	public void processMsg(HttpServletResponse reponse){
		
		Continuation contin = this.getContinuation();
		this.pt = new PullThread("send-thread",reponse);
		pt.start();
		contin.suspend();
	}
	
	 class TimeMessage extends Thread{
		
		private long connectionTime =0;
		
    	public TimeMessage(String name){
    		this.setName(name);
    		this.connectionTime = System.currentTimeMillis();
    	}
    	
    	public void run(){
    		long i = 1;
	    		while(i<2333){
		    			//
		    			
		    			i++;
		    			
		    			
	    		}
    	}
    	
    	public void stopThread() {
			interrupt();
		} 
    	
		
	}
	 
	 
	 class PullThread extends Thread{
			
		    private HttpServletResponse reponse;
		    
		    private volatile boolean workFlag = true;
			
	    	public PullThread(String name,HttpServletResponse reponse){
	    		this.setName(name);
	    		this.reponse = reponse;
	    	}
	    	
	    	public void run(){
	    		int i = 1;
	    		while(true){
		    		try {
		    			while(queue.size()>0){
							String ms;
							try {
								ms = queue.take();
								System.out.println("start send the"+msgNum+" msg to client");
								this.sendEvent(reponse,ms);
								msgNum++;
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}
		    			this.workFlag = false;
		    			synchronized(this){
		    				System.out.println("hi me is sleep wait for wakeup");
		    				this.wait();
		    				System.out.println("hi me is waked by notice");
		    				
		    			}
		    			
					} catch (Exception e) {
						e.printStackTrace();
					} 
	    		}
	    		
	    	}
	    	
	    	public void sendEvent(HttpServletResponse reponse,String msg) throws IOException{
	    		
	    		reponse.getWriter().write(msg);
	    		reponse.getWriter().flush();
	    	}
	    	
	    	
	    	public void startup(){
	    		if(!this.workFlag){
	    			System.out.println("pull thread is sleeping, to wakeup");
		    		synchronized(this){
		    			this.notify();
		    			this.workFlag = true;
		    		}
	    		}else{
	    			System.out.println("pull thread is working,not to wakeup");
	    		}
	    		
	    	}
			
		}
	
	
}
