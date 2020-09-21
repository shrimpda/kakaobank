package com.kakaobank.server.transport;

import com.kakaobank.common.io.StreamInput;
import com.kakaobank.server.configs.Config;
import com.kakaobank.server.transport.netty.NettyChannel;
import io.netty.channel.ChannelHandlerContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.function.Function;

public interface Transport {

	Config<String> HOST = new Config<>("transport.host", configs -> "localhost",
		Function.identity());

	Config<Integer> PORT = new Config<>("transport.port", configs -> "7001",
		str -> Integer.parseInt(str));

	void start();

	void setTransportService(TransportService transportService);

	void messageReceived(NettyChannel channel, StreamInput streamInput) throws IOException;

	void exceptionCaught(ChannelHandlerContext ctx, Throwable cause);

	NettyChannel connectToNode(InetSocketAddress address) throws IOException;
}
