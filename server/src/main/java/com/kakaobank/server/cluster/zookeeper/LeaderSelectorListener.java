package com.kakaobank.server.cluster.zookeeper;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;

public class LeaderSelectorListener extends LeaderSelectorListenerAdapter {

	private final LeaderSelector leaderSelector;
	private final AtomicInteger leaderCount = new AtomicInteger();

	public LeaderSelectorListener(CuratorFramework client, String path) {
		this.leaderSelector = new LeaderSelector(client, path, this);

		leaderSelector.autoRequeue();
	}

	public void start() {
		leaderSelector.start();
	}

	@Override
	public void takeLeadership(CuratorFramework client) throws Exception {
		final int         waitSeconds = (int)(5 * Math.random()) + 1;

		System.out.println("aaa is now the leader. Waiting " + waitSeconds + " seconds...");
		System.out.println("aaa has been leader " + leaderCount.getAndIncrement() + " time(s) before.");
		try
		{
			Thread.sleep(TimeUnit.SECONDS.toMillis(waitSeconds));
		}
		catch ( InterruptedException e )
		{
			System.err.println("aaa was interrupted.");
			Thread.currentThread().interrupt();
		}
		finally
		{
			System.out.println("aaa relinquishing leadership.\n");
		}
	}
}
