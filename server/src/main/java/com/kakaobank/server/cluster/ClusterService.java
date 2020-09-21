package com.kakaobank.server.cluster;

import com.kakaobank.server.cluster.zookeeper.ZookeeperClusterService;
import com.kakaobank.server.configs.Configs;
import com.kakaobank.server.service.Service;
import com.kakaobank.server.transport.TransportService;
import java.io.IOException;

public class ClusterService implements Service {

	private final ZookeeperClusterService zookeeperClusterService;

	public ClusterService(Configs configs, TransportService transportService) {
		this.zookeeperClusterService = new ZookeeperClusterService(configs, transportService);
	}

	@Override
	public void start() throws IOException {
		zookeeperClusterService.start();
	}

	@Override
	public void stop() {

	}
}
