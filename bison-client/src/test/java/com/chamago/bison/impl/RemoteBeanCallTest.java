/**
 * 
 */
package com.chamago.bison.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.JUnit4;
import org.junit.runner.RunWith;

import com.chamago.bison.BisonContext;
/**
 * @author Gavin.peng
 * 
 * 2013-10-9 下午03:58:35
 × bison-app
 */
@RunWith(JUnit4.class)
public class RemoteBeanCallTest {
	
	private BisonContext context;

	private long totalTime;
	
	@Before
	public void init() {
		
		System.setProperty("conf.dir", "E:\\cmg-projects\\bison-client\\conf");
		context = new BisonContext(
				"E:\\cmg-projects\\bison-client\\conf\\config.xml");
		sleep(2000);
		System.out.println("==============init finished===================");
	}

	@After
	public void clean() {
		context = null;
	}

	
	@Test
	public  void testRemoteBeanCall(){
		User u = new User(); 
		u.setPassword("111111111");
		u.setUserName("pengrongxin");
		for(int i=0;i<10;i++){
			long st = System.currentTimeMillis();
			 RemoteBeanCallUtil.RemoteBeanCall(u, "1", "login",context);
			 long et = System.currentTimeMillis();
			 System.out.println("send ok,time:"+(et-st));
		}
	}

	@Test
	public  void testRemoteDefaultBeanCall(){
		User u = new User(); 
		u.setPassword("111111111");
		u.setUserName("pengrongxin");
		for(int i=0;i<10;i++){
			long st = System.currentTimeMillis();
			RemoteBeanCallUtil.RemoteBeanCall(u, "1",null,context);
			long et = System.currentTimeMillis();
			System.out.println("send ok,time:"+(et-st));
		}
	}
	
	@Test
	@Ignore
	public  void testRemoteMuiltMethodCall(){
		User u = new User(); 
		u.setPassword("111111111");
		u.setUserName("pengrongxin");
		long st = System.currentTimeMillis();
		RemoteBeanCallUtil.RemoteBeanCall(u, "1","test2,test1,test3",context);
		long et = System.currentTimeMillis();
		System.out.println("send ok,time:"+(et-st));
	}
	
	@Test
	@Ignore
	public  void testRemoteNoParamsMethodCall(){
		User u = new User(); 
		u.setPassword("111111111");
		u.setUserName("pengrongxin");
		long st = System.currentTimeMillis();
		RemoteBeanCallUtil.RemoteBeanCall(u, "1","noParamsMethod",context);
		long et = System.currentTimeMillis();
		System.out.println("send ok,time:"+(et-st));
	}
	
	@Test
	@Ignore
	public void testBatchInterfaceStubBeanCall() throws RemoteException {

		User u = new User(); 
		u.setPassword("111111111");
		u.setUserName("pengrongxin");
		//for(int i=0;i<5;i++){
			long st = System.currentTimeMillis();
			System.out.println("stime,time:"+st);
			 RemoteBeanCallUtil.RemoteBeanCall(u, "1", "login",context);
			 long et = System.currentTimeMillis();
			 //totalTime+=(et - st);
			 System.out.println("send ok,rtt:"+(et-st)+",end time:"+et);
			 
		int threadNums = 100;
		
		List<Thread> tList = new ArrayList<Thread>();
		for (int i = 1; i <= threadNums; i++) {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					User u = new User(); 
					u.setPassword("111111111");
					u.setUserName("pengrongxin");
					//for(int i=0;i<10;i++){
						long st = System.currentTimeMillis();
						System.out.println("stime,time:"+st);
						 RemoteBeanCallUtil.RemoteBeanCall(u, "1", "login",context);
						 long et = System.currentTimeMillis();
						 totalTime+=(et - st);
						 System.out.println("send ok,rtt:"+(et-st)+",end time:"+et);
						 //sleep(2000);
					//}
				}
			},"work-thread-"+i);
			tList.add(t);
		}
		for (Thread t : tList) {
			t.start();
		}
		for (Thread t : tList) {
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("thread nums:"+threadNums+",avg rtt:"+(totalTime/threadNums));
	}

	
	public void sleep(int time){
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void testRunnable(){
		
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				User u = new User(); 
				u.setPassword("111111111");
				u.setUserName("pengrongxin");
				//for(int i=0;i<10;i++){
					long st = System.currentTimeMillis();
					System.out.println("stime,time:"+st);
					 RemoteBeanCallUtil.RemoteBeanCall(u, "1", "login",context);
					 long et = System.currentTimeMillis();
					 totalTime+=(et - st);
					 System.out.println("send ok,rtt:"+(et-st)+",end time:"+et);
					 //sleep(2000);
				//}
			}
		},"work-thread-"+0);
	}
	
	
	
}
