/**
 * 
 */
package com.chamago.cometserver.connection;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * 
 * @author zhenzi
 * 2011-8-9 下午03:05:04
 */
public class NamedThreadFactory implements ThreadFactory
{
	static final AtomicInteger poolNumber = new AtomicInteger(1);

	final AtomicInteger threadNumber = new AtomicInteger(1);
	final ThreadGroup group;
	final String namePrefix;
	final boolean isDaemon;

	public NamedThreadFactory()
	{
		this("pool-msg-pull");
	}

	public NamedThreadFactory(String name)
	{
		this(name, false);
	}
	
	public NamedThreadFactory(String preffix, boolean daemon)
	{
		SecurityManager s = System.getSecurityManager();
		group = (s != null) ? s.getThreadGroup() : Thread.currentThread()
				.getThreadGroup();
		namePrefix = preffix + "-" + poolNumber.getAndIncrement() + "-thread-";
		isDaemon = daemon;
	}

	public Thread newThread(Runnable r)
	{
		Thread t = new Thread(group, r, namePrefix
				+ threadNumber.getAndIncrement(), 0);
		t.setDaemon(isDaemon);
		if (t.getPriority() != Thread.NORM_PRIORITY)
		{
			t.setPriority(Thread.NORM_PRIORITY);
		}
		return t;

	}
}
