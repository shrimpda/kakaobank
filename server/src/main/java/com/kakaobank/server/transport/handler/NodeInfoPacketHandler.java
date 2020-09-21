package com.kakaobank.server.transport.handler;

import com.kakaobank.common.io.StreamOutput;
import com.kakaobank.server.node.NodeInfo;
import com.kakaobank.server.transport.ConnectionManager;
import com.kakaobank.server.transport.TransportService;
import com.kakaobank.server.transport.handler.packet.NodeInfoPacket;
import com.kakaobank.server.transport.netty.NettyChannel;
import java.io.IOException;

public class NodeInfoPacketHandler extends PacketHandler<NodeInfoPacket> {

	public static final String NAME = "node_info";

	private final TransportService transportService;

	public NodeInfoPacketHandler(TransportService transportService) {
		super(NodeInfoPacket::new);
		this.transportService = transportService;
	}

	@Override
	public void execute(NodeInfoPacket packet, NettyChannel channel) throws IOException {
		NodeInfo nodeInfo = packet.getNodeInfo();

		if (!transportService.nodeConnected(nodeInfo)) {
			transportService.addConnectedNode(nodeInfo, channel);

			NodeInfo localNode = transportService.getLocalNode();
			NodeInfoPacket nodeInfoPacket = new NodeInfoPacket(localNode);

			try (StreamOutput streamOutput = new StreamOutput()) {
				streamOutput.writeString(NodeInfoPacketHandler.NAME);
				nodeInfoPacket.writeTo(streamOutput);

				channel.write(streamOutput.bytes());
			}
		}
	}
}
