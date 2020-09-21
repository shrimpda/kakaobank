package com.kakaobank.server.transport;

import com.kakaobank.server.configs.Configs;
import com.kakaobank.server.node.NodeInfo;
import com.kakaobank.server.transport.netty.NettyChannel;
import com.kakaobank.server.util.IOUtil;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionManager {

	public static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

	private final ConcurrentMap<NodeInfo, NettyChannel> connectedNodes = new ConcurrentHashMap<>();

	private final Configs configs;
	private final Transport transport;
	private final DelegatingNodeConnectionListener connectionListener = new DelegatingNodeConnectionListener();

	public ConnectionManager(Configs configs, Transport transport) {
		this.configs = configs;
		this.transport = transport;
	}

	public void connectToNode(InetSocketAddress address) throws IOException {
		if (connectedNodes.keySet().stream().map(NodeInfo::getAddress).anyMatch(address::equals)) {
			return;
		}

		NettyChannel channel = transport.connectToNode(address);

		CountDownLatch countDownLatch = new CountDownLatch(1);
		channel.addConnectListener(future -> {
			if (future.isSuccess()) {
				countDownLatch.countDown();
			} else {
				Throwable cause = future.cause();
				logger.info(cause.getMessage(), cause);
			}
		});

		ScheduledExecutorService executors = Executors.newScheduledThreadPool(1);
		executors.schedule(() -> {
			if (countDownLatch.getCount() == 1) {
				IOUtil.closeQuietly(channel);
			}
		}, 10, TimeUnit.SECONDS);
	}

	public void disConnectToNode(InetSocketAddress address) {
		Optional<NodeInfo> nodeInfo = connectedNodes.keySet().stream().filter(address::equals).findFirst();
		nodeInfo.ifPresent(node -> {
			NettyChannel channel = connectedNodes.get(node);
			if (channel != null) {
				IOUtil.closeQuietly(channel);
			}
		});
	}

	public void addConnectedNode(NodeInfo nodeInfo, NettyChannel nettyChannel) {
		if (connectedNodes.putIfAbsent(nodeInfo, nettyChannel) != null) {
			logger.debug("existing connection to node [{}]", nodeInfo.getName());
			IOUtil.closeQuietly(nettyChannel);
		} else {
			logger.info("connected to node [{}]", nodeInfo.getName());
			connectionListener.onNodeConnected(nodeInfo, nettyChannel);
			nettyChannel.addCloseListener(future -> {
				connectedNodes.remove(nodeInfo, nettyChannel);
				connectionListener.onNodeDisconnected(nodeInfo, nettyChannel);
			});
		}
	}

	public void addListener(TransportConnectionListener listener) {
		this.connectionListener.listeners.addIfAbsent(listener);
	}

	public void removeListener(TransportConnectionListener listener) {
		this.connectionListener.listeners.remove(listener);
	}

	public boolean nodeConnected(NodeInfo nodeInfo) {
		return connectedNodes.containsKey(nodeInfo);
	}

	public List<InetSocketAddress> getConnectedList() {
		return connectedNodes.values().stream().map(c -> {
			return (InetSocketAddress) c.channel().localAddress();
		}).collect(Collectors.toList());
	}

	private static final class DelegatingNodeConnectionListener implements TransportConnectionListener {

		private final CopyOnWriteArrayList<TransportConnectionListener> listeners = new CopyOnWriteArrayList<>();

		@Override
		public void onNodeDisconnected(NodeInfo node, NettyChannel channel) {
			for (TransportConnectionListener listener : listeners) {
				listener.onNodeDisconnected(node, channel);
			}
		}

		@Override
		public void onNodeConnected(NodeInfo node, NettyChannel channel) {
			for (TransportConnectionListener listener : listeners) {
				listener.onNodeConnected(node, channel);
			}
		}
	}
}
