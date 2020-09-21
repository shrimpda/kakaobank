package com.kakaobank.server.cluster.zookeeper;

import com.kakaobank.server.configs.Config;
import com.kakaobank.server.configs.Configs;
import com.kakaobank.server.service.Service;
import com.kakaobank.server.transport.TransportService;
import java.io.IOException;
import java.util.function.Function;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZookeeperClusterService implements Service {

	public static final Logger logger = LoggerFactory.getLogger(ZookeeperClusterService.class);

	public static final Config<String> HOST = new Config<>("zookeeper.host", configs -> "localhost:2181",
		Function.identity());

	public static final Config<Integer> CONNECTION_TIMEOUT = new Config<>("zookeeper.connection.timeout", configs -> "3000",
		str -> Integer.parseInt(str));

	private final ZookeeperClient client;
	private final ZookeeperClusterManager clusterManager;

	public ZookeeperClusterService(Configs configs, TransportService transportService) {
		String host = HOST.get(configs);
		int sessionTimeout = CONNECTION_TIMEOUT.get(configs);

		this.client = new ZookeeperClient(configs, transportService, host, sessionTimeout, new ClusterManagerWatcher());
		this.clusterManager = new ZookeeperClusterManager(client, transportService);
	}

	@Override
	public void start() throws IOException {
		logger.info("Zookeeper cluster service start.");

		this.client.connect();
		clusterManager.start();
	}

	@Override
	public void stop() {

	}

	class ClusterManagerWatcher implements ZookeeperEventWatcher {

		@Override
		public void process(WatchedEvent event) {
			logger.debug("Process Zookeeper Event({})", event);

			EventType eventType = event.getType();

			if (client.isConnected()) {
				if (eventType == EventType.NodeChildrenChanged) {
					String path = event.getPath();

					clusterManager.handleAndRegisterWatcher(path);
				}
			}
		}

		@Override
		public boolean connected() {
			clusterManager.handleAndRegisterWatcher("/server/");

			return true;
		}

		@Override
		public boolean disConnected() {
			return true;
		}
	}
}
