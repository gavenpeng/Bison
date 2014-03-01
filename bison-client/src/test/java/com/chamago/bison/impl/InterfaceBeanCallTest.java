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
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.chamago.bison.BisonContext;
import com.chamago.bison.RemoteObjectFactory;
import com.chamago.bison.stub.IUserService;

/**
 * @author Gavin.peng
 * 
 *         2013-10-9 下午04:01:01 × bison-app
 */
@RunWith(JUnit4.class)
public class InterfaceBeanCallTest {

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
	@Ignore
	public void testBatchInterfaceStubBeanCall() throws RemoteException {
		interfaceStubBeanCall();
		totalTime = 0;
		int threadNums = 100;
		List<Thread> tList = new ArrayList<Thread>();
		for (int i = 1; i <= threadNums; i++) {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						interfaceStubBeanCall();
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
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

	@Test
	public void testInterfaceStubBeanCall() throws RemoteException {
		for (int i = 0; i < 2; i++) {
			long st = System.currentTimeMillis();
			IUserService userS = (IUserService) RemoteObjectFactory
					.findRemoteObject(IUserService.class, "1", context);
			if (userS != null) {
				List<User> uList = userS.findUsers("安睡宝");
				long et = System.currentTimeMillis();
				//System.out.println("thread:"+Thread.currentThread().getName()+",num "+i+",RTT:" + (et - st));
				if (uList != null && uList.size() > 0) {
					for (User u : uList) {
						System.out.println("username:" + u.getUserName()
								+ ",password:" + u.getPassword());
					}
				} else {
					System.out.println("not find user by nick");
				}
			}

		}
	}
	
	
	public void interfaceStubBeanCall() throws RemoteException {
		for (int i = 0; i < 50; i++) {
			long st = System.currentTimeMillis();
			IUserService userS = (IUserService) RemoteObjectFactory
					.findRemoteObject(IUserService.class, "1", context);
			if (userS != null) {
				List<User> uList = userS.findUsers("安睡宝-"+0);
				long et = System.currentTimeMillis();
				totalTime+=(et - st);
				System.out.println("thread:"+Thread.currentThread().getName()+",num "+0+",RTT:" + (et - st));
				if (uList != null && uList.size() > 0) {
//					for (User u : uList) {
//						System.out.println("thread:"+Thread.currentThread().getName()+",username:" + u.getUserName()
//								+ ",password:" + u.getPassword());
//					}
				} else {
					System.out.println("not find user by nick");
				}

			}

		}
	}
	
	public void sleep(int time){
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
