package com.kakaobank.server.cluster.zookeeper;

import org.apache.zookeeper.Watcher;

public interface ZookeeperEventWatcher extends Watcher {

	boolean connected();

	boolean disConnected();
}
