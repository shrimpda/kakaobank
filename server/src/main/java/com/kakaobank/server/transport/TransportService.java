package com.kakaobank.server.transport;

import com.kakaobank.server.configs.Configs;
import com.kakaobank.server.file.FileRepository;
import com.kakaobank.server.node.Node;
import com.kakaobank.server.node.NodeInfo;
import com.kakaobank.server.service.Service;
import com.kakaobank.server.transport.handler.FileCommandPacketHandler;
import com.kakaobank.server.transport.handler.FileInfoPacketHandler;
import com.kakaobank.server.transport.handler.IdentityPacketHandler;
import com.kakaobank.server.transport.handler.NodeInfoPacketHandler;
import com.kakaobank.server.transport.handler.PacketHandler;
import com.kakaobank.server.transport.netty.NettyChannel;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransportService implements Service {

	public static final Logger logger = LoggerFactory.getLogger(TransportService.class);

	private final Configs configs;
	private final Transport transport;
	private final ConnectionManager connectionManager;
	private final Map<String, PacketHandler> packetHandlerMap = new ConcurrentHashMap<>();

	private final FileRepository fileRepository;

	volatile NodeInfo localNode;

	public TransportService(Configs configs, Transport transport, ConnectionManager connectionManager, FileRepository fileRepository) {
		this.configs = configs;
		this.transport = transport;
		this.connectionManager = connectionManager;
		this.fileRepository = fileRepository;

		initializePacketHandler();
	}

	@Override
	public void start() {
		int port = Transport.PORT.get(configs);
		String host = Transport.HOST.get(configs);
		localNode = new NodeInfo(Node.NODE_NAME.get(configs), InetSocketAddress.createUnresolved(host, port));
		transport.setTransportService(this);
		transport.start();
	}

	@Override
	public void stop() {

	}

	public NodeInfo getLocalNode() {
		return localNode;
	}

	public List<InetSocketAddress> getConnectedList() {
		return connectionManager.getConnectedList();
	}

	public void connectToNode(InetSocketAddress address) throws IOException {
		if (localNode.getAddress().equals(address)) {
			return;
		}

		connectionManager.connectToNode(address);
	}

	public void disConnectToNode(InetSocketAddress address) {
		connectionManager.disConnectToNode(address);
	}

	public void addConnectedNode(NodeInfo nodeInfo, NettyChannel nettyChannel) {
		connectionManager.addConnectedNode(nodeInfo, nettyChannel);
	}

	public void addConnectionListener(TransportConnectionListener listener) {
		connectionManager.addListener(listener);
	}

	public void removeConnectionListener(TransportConnectionListener listener) {
		connectionManager.removeListener(listener);
	}

	public boolean nodeConnected(NodeInfo nodeInfo) {
		return isLocalNode(nodeInfo) || connectionManager.nodeConnected(nodeInfo);
	}

	private boolean isLocalNode(NodeInfo nodeInfo) {
		return localNode.equals(nodeInfo);
	}

	public PacketHandler getPacketHandler(String type) {
		return packetHandlerMap.get(type);
	}

	private void initializePacketHandler() {
		packetHandlerMap.put(IdentityPacketHandler.NAME, new IdentityPacketHandler(configs, this));
		packetHandlerMap.put(FileInfoPacketHandler.NAME, new FileInfoPacketHandler(configs,this, fileRepository));
		packetHandlerMap.put(FileCommandPacketHandler.NAME, new FileCommandPacketHandler(fileRepository));
		packetHandlerMap.put(NodeInfoPacketHandler.NAME, new NodeInfoPacketHandler(this));
	}
}
