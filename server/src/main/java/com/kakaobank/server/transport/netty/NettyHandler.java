package com.kakaobank.server.transport.netty;

import com.kakaobank.common.io.StreamInput;
import com.kakaobank.server.transport.Transport;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;

public class NettyHandler extends ChannelDuplexHandler {

	private final Transport transport;

	public NettyHandler(Transport transport) {
		this.transport = transport;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf buf = (ByteBuf) msg;

		StreamInput streamInput = new StreamInput(buf);

		try {
			Attribute<NettyChannel> channelAttribute = ctx.channel().attr(NettyTransport.CHANNEL_KEY);
			transport.messageReceived(channelAttribute.get(), streamInput);
		} finally {
			buf.release();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		transport.exceptionCaught(ctx, cause);
	}
}
