package com.kakaobank.server.transport.netty;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import java.io.Closeable;
import java.io.IOException;

public class NettyChannel implements Closeable {

	private final Channel channel;
	private final ChannelFuture connectFuture;
	private final ChannelFuture closeFuture;

	NettyChannel(Channel channel, ChannelFuture connectFuture) {
		this.channel = channel;
		this.connectFuture = connectFuture;
		this.closeFuture = channel.closeFuture();
	}

	public Channel channel() {
		return channel;
	}

	@Override
	public void close() throws IOException {
		channel.close();
	}

	public void write(byte[] msg) {
		channel.writeAndFlush(Unpooled.wrappedBuffer(msg));
	}

	public void addConnectListener(ChannelFutureListener listener) {
		connectFuture.addListener(listener);
	}

	public void addCloseListener(ChannelFutureListener listener) {
		closeFuture.addListener(listener);
	}
}
