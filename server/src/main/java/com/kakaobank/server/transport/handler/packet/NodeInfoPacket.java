package com.kakaobank.server.transport.handler.packet;

import com.kakaobank.common.io.StreamInput;
import com.kakaobank.common.io.StreamOutput;
import com.kakaobank.common.packet.TransportPacket;
import com.kakaobank.server.node.NodeInfo;
import java.io.IOException;

public class NodeInfoPacket extends TransportPacket {

	private NodeInfo nodeInfo;

	public NodeInfoPacket() {

	}

	public NodeInfoPacket(NodeInfo nodeInfo) {
		this.nodeInfo = nodeInfo;
	}

	public NodeInfo getNodeInfo() {
		return nodeInfo;
	}

	@Override
	public void readFrom(StreamInput streamInput) throws IOException {
		this.nodeInfo = new NodeInfo(streamInput);
	}

	@Override
	public void writeTo(StreamOutput streamOutput) throws IOException {
		nodeInfo.writeTo(streamOutput);
	}
}
