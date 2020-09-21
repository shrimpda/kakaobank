package com.kakaobank.server.transport.netty;

import com.kakaobank.common.io.StreamInput;
import com.kakaobank.common.transport.file.EncryptChunkedFile;
import com.kakaobank.server.file.FileWriter;
import com.kakaobank.server.transport.TransportService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class ChunkedFileHandler extends ChannelDuplexHandler {

	private TransportService transportService;
	private FileWriter fileWriter;

	public ChunkedFileHandler(TransportService transportService, FileWriter fileWriter) {
		this.transportService = transportService;
		this.fileWriter = fileWriter;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf buf = (ByteBuf) msg;

		int length = buf.readInt();
		byte[] bytes = new byte[length];
		buf.readBytes(bytes);

		String state = new String(bytes);

		if (EncryptChunkedFile.TRANSFER.equals(state)) {
			fileWriter.write(buf);
		} else if (EncryptChunkedFile.END.equals(state)) {
			fileWriter.close();
		}

		buf.release();
	}
}
