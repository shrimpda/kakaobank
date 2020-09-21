package com.kakaobank.common.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.io.StreamCorruptedException;
import java.util.List;

public class SizeDecoder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
		throws Exception {
		while (in.readableBytes() >= (Header.HEADER_LENGTH)) {
			if (in.readableBytes() < (Header.HEADER_LENGTH)) {
				break;
			}
			int messageLength = getMessageLength(in);
			if (messageLength == -1) {
				break;
			}

			int messageLengthWithHeader = messageLength + Header.HEADER_LENGTH;
			if (messageLengthWithHeader > in.readableBytes()) {
				break;
			} else {
				final int readerIndex = in.readerIndex();

				final ByteBuf message = in
					.retainedSlice(readerIndex + Header.HEADER_LENGTH, messageLength);
				out.add(message);
				in.readerIndex(readerIndex + messageLengthWithHeader);
			}
		}
	}

	private static int getMessageLength(ByteBuf in) throws StreamCorruptedException {
		if (in.readableBytes() < Header.HEADER_LENGTH) {
			return -1;
		}

		if (in.getByte(0) != Header.MAGIC_NUMBER[0]
			|| in.getByte(1) != Header.MAGIC_NUMBER[1]
			|| in.getByte(2) != Header.MAGIC_NUMBER[2]
			|| in.getByte(3) != Header.MAGIC_NUMBER[3]
			|| in.getByte(4) != Header.MAGIC_NUMBER[4]) {
			String magicNumber = "["
				+ Integer.toHexString(in.getByte(0) & 0xFF) + ", "
				+ Integer.toHexString(in.getByte(1) & 0xFF) + ", "
				+ Integer.toHexString(in.getByte(2) & 0xFF) + ", "
				+ Integer.toHexString(in.getByte(3) & 0xFF) + ", "
				+ Integer.toHexString(in.getByte(4) & 0xFF)
				+ "]";
			throw new StreamCorruptedException("invalid magic number " + magicNumber);
		}

		final int messageLength = in.getInt(Header.MAGIC_NUMBER_LENGTH);

		return messageLength;
	}
}
