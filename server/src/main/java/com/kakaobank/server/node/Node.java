package com.kakaobank.server.node;

import com.codahale.metrics.MetricRegistry;
import com.kakaobank.server.cluster.ClusterService;
import com.kakaobank.server.configs.Config;
import com.kakaobank.server.configs.Configs;
import com.kakaobank.server.file.FileRepository;
import com.kakaobank.server.metic.ApplicationMetric;
import com.kakaobank.server.transport.ConnectionManager;
import com.kakaobank.server.transport.Transport;
import com.kakaobank.server.transport.TransportService;
import com.kakaobank.server.transport.netty.NettyTransport;
import java.io.IOException;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Node {

	public static final Config<String> NODE_NAME = new Config<>("node.name", configs -> "node1",
		Function.identity());

	private static final Logger logger = LoggerFactory.getLogger(Node.class);

	private final Configs configs;
	private final TransportService transportService;
	private final ClusterService clusterService;
	private final ApplicationMetric metrics;

	public Node(Configs configs) {
		this.configs = configs;
		MetricRegistry metricRegistry = new MetricRegistry();
		this.metrics = new ApplicationMetric(metricRegistry);
		Transport transport = new NettyTransport(configs);
		FileRepository fileRepository = new FileRepository(configs);
		ConnectionManager connectionManager = new ConnectionManager(configs, transport);
		this.transportService = new TransportService(configs, transport, connectionManager, fileRepository);
		this.clusterService = new ClusterService(configs, transportService);
	}

	public void start() throws IOException {
		String nodeName = NODE_NAME.get(configs);

		logger.info("Node [{}] start", nodeName);
		transportService.start();
		clusterService.start();

		logger.info("Node [{}] has been started", nodeName);
	}
}
