package com.kakaobank.server.transport;

import com.kakaobank.server.node.NodeInfo;
import com.kakaobank.server.transport.netty.NettyChannel;

public interface TransportConnectionListener {

	default void onNodeConnected(NodeInfo nodeInfo, NettyChannel channel) {}

	default void onNodeDisconnected(NodeInfo nodeInfo, NettyChannel channel) {}
}
