package com.kakaobank.common.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class HeaderEncoder extends MessageToByteEncoder<ByteBuf> {

	@Override
	protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
		out.writeBytes(Header.MAGIC_NUMBER);
		int readable = msg.readableBytes();
		out.writeInt(readable);
		out.writeBytes(msg, 0, readable);
	}
}
