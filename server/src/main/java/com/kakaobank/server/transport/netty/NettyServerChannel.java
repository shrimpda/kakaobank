package com.kakaobank.server.transport.netty;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import java.io.Closeable;
import java.io.IOException;

public class NettyServerChannel implements Closeable {

	private final Channel channel;
	private final ChannelFuture closeFuture;

	NettyServerChannel(Channel channel) {
		this.channel = channel;
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

	public void addCloseListener(ChannelFutureListener listener) {
		closeFuture.addListener(listener);
	}
}