package com.kakaobank.server.cluster.zookeeper;

import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZookeeperConnectionStateListener implements ConnectionStateListener {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final AtomicBoolean connected = new AtomicBoolean(false);

	private final ZookeeperEventWatcher zookeeperEventWatcher;

	public ZookeeperConnectionStateListener(ZookeeperEventWatcher zookeeperEventWatcher) {
		this.zookeeperEventWatcher = zookeeperEventWatcher;
	}

	@Override
	public void stateChanged(CuratorFramework client, ConnectionState newState) {
		if (newState.isConnected()) {
			boolean changed = connected.compareAndSet(false, true);
			if (changed) {
				logger.info("State change to connect");
				boolean result = zookeeperEventWatcher.connected();
			}
		} else {
			boolean changed = connected.compareAndSet(true, false);
			if (changed) {
				logger.info("State change to disconnect");
				boolean result = zookeeperEventWatcher.disConnected();
			}
		}
	}
}
