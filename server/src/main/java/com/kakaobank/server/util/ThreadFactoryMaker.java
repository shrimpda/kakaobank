package com.kakaobank.server.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadFactoryMaker {

	public static ThreadFactory create(String name) {
		return new NewThreadFactory(name);
	}

	static class NewThreadFactory implements ThreadFactory {

		final ThreadGroup group;
		final AtomicInteger threadNumber = new AtomicInteger(1);
		final String name;

		NewThreadFactory(String name) {
			this.name = name;
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() :
				Thread.currentThread().getThreadGroup();
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r,
				name + "[T#" + threadNumber.getAndIncrement() + "]",
				0);
			t.setDaemon(true);
			return t;
		}
	}
}
