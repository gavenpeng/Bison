package com.chamago.cometserver.connection;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;

/**
 * 消息下发任务工厂
 * @author Gavin.Peng
 * 2014-2-24 下午03:06:53
 */
public class StreamMsgPullFactory {
	private int minThreads;
	private int maxThreads;
	private int queueSize;
	private ThreadPoolExecutor threadPool;
	//标示是否关闭
	private boolean isShutDown = false;
	public StreamMsgPullFactory(int minThreads,int maxThreads,int queueSize,RejectedExecutionHandler rejectHandler){
		if(minThreads <= 0 || maxThreads <= 0 || queueSize <= 0){
			throw new RuntimeException("minThread,maxThread and queueSize must large than 0");
		}
		this.minThreads = minThreads;
		this.maxThreads = maxThreads;
		this.queueSize = queueSize;
		
		threadPool = new ThreadPoolExecutor(this.minThreads, this.maxThreads,
    			60, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(this.queueSize),
				new NamedThreadFactory("pool-msg-pull",true),rejectHandler == null ? new AbortPolicy() : rejectHandler);
	}
	public void commit(Runnable task)throws RejectedExecutionException,NullPointerException {
		if(!isShutDown){
			threadPool.execute(task);
		}
	}
	public void shutdown(){
		isShutDown = true;
		try{
			threadPool.shutdown();
		}catch(Exception e){};//关闭的过程中抛出的异常忽略
	}
}
