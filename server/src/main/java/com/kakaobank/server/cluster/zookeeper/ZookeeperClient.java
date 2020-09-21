package com.kakaobank.server.cluster.zookeeper;

import com.kakaobank.server.configs.Configs;
import com.kakaobank.server.transport.TransportService;
import java.util.Collections;
import java.util.List;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZookeeperClient {

	private static final Logger logger = LoggerFactory.getLogger(ZookeeperClient.class);

	private final ZookeeperConnectionManager connectionManager;
	private final ZookeeperEventWatcher eventWatcher;

	public ZookeeperClient(Configs configs, TransportService transportService, String host, int timeout, ZookeeperEventWatcher eventWatcher) {
		this.connectionManager = new ZookeeperConnectionManager(configs, transportService, host, timeout, eventWatcher);
		this.eventWatcher = eventWatcher;
	}

	public void connect() {
		logger.debug("connect() started");

		connectionManager.start();
	}

	public void createPath(String value) throws Exception {
		String path = getPath(value, false);

		logger.debug("createPath() started. value:{}, path:{}", value, path);

		CuratorFramework client = connectionManager.getClient();

		Stat stat = client.checkExists().forPath(path);

		if (stat == null) {
			try {
				client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
			} catch (KeeperException.NodeExistsException nodeExists) {
				// skip
			}
		}
	}

	public byte[] getData(String path) throws Exception {
		return getData(path, false);
	}

	public byte[] getData(String path, boolean watch) throws Exception {
		logger.debug("getData() started. path:{}, watch:{}", path, watch);

		CuratorFramework client = connectionManager.getClient();

		if (watch) {
			byte[] bytes = client.getData().usingWatcher(eventWatcher).forPath(path);
			return bytes;
		} else {
			byte[] bytes = client.getData().forPath(path);
			return bytes;
		}
	}

	private String getPath(String value, boolean includeEndPath) {
		if (value.length() == 1 && value.charAt(0) == '/') {
			return value;
		}

		if (value.charAt(value.length() - 1) == '/') {
			return value.substring(0, value.length() - 1);
		}

		if (includeEndPath) {
			return value;
		} else {
			ZKPaths.PathAndNode pathAndNode = ZKPaths.getPathAndNode(value);
			return pathAndNode.getPath();
		}
	}

	public List<String> getChildNodeList(String value, boolean watch) throws Exception {
		String path = getPath(value, true);

		logger.debug("getChildNodeList() started. path:{}, watch:{}", path, watch);

		try {
			CuratorFramework client = connectionManager.getClient();

			if (watch) {
				List<String> childList = client.getChildren().usingWatcher(eventWatcher).forPath(path);

				return childList;
			} else {
				List<String> childList = client.getChildren().forPath(path);
				return childList;
			}
		} catch (KeeperException.NoNodeException noNode) {
			// skip
		}

		return Collections.emptyList();
	}

	public boolean isConnected() {
		if (!connectionManager.isConnected()) {
			return false;
		}

		return true;
	}
}
