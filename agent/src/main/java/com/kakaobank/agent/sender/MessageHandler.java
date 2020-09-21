package com.kakaobank.agent.sender;

import com.kakaobank.agent.console.ConsoleLogger;
import com.kakaobank.common.crypt.AesCrypt;
import com.kakaobank.common.file.FileInfo;
import com.kakaobank.common.io.StreamInput;
import com.kakaobank.common.io.StreamOutput;
import com.kakaobank.common.packet.FileCommandPacket;
import com.kakaobank.common.packet.FileCommandPacket.Command;
import com.kakaobank.common.packet.FileInfoPacket;
import com.kakaobank.common.packet.IdentityPacket;
import com.kakaobank.common.packet.ServerInfoPacket;
import com.kakaobank.common.transport.file.EncryptChunkedFile;
import com.kakaobank.common.type.Type;
import com.kakaobank.common.unit.SizeValue;
import com.twmacinta.util.MD5;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

public class MessageHandler extends ChannelDuplexHandler {

	private final File file;
	private final AtomicBoolean done;
	private String password;

	public MessageHandler(File file, AtomicBoolean done) {
		this.file = file;
		this.done = done;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		IdentityPacket identityPacket = new IdentityPacket(Type.AGENT);
		try (StreamOutput streamOutput = new StreamOutput()) {
			streamOutput.writeString("identity");
			identityPacket.writeTo(streamOutput);

			ctx.writeAndFlush(Unpooled.wrappedBuffer(streamOutput.bytes()));
		}

		super.channelActive(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		final ByteBuf byteBuf = (ByteBuf) msg;

		StreamInput streamInput = new StreamInput(byteBuf);

		String type = streamInput.readString();

		if ("server_info".equals(type)) {
			ServerInfoPacket serverInfoPacket = new ServerInfoPacket();
			serverInfoPacket.readFrom(streamInput);

			password = serverInfoPacket.getPassword();

			String hash = MD5.asHex(MD5.getHash(file));
			final FileInfo fileInfo = new FileInfo(file.getName(), file.length(), hash);

			FileInfoPacket fileInfoPacket = new FileInfoPacket(fileInfo);

			try (StreamOutput streamOutput = new StreamOutput()) {
				streamOutput.writeString("file_info");
				fileInfoPacket.writeTo(streamOutput);

				ctx.writeAndFlush(Unpooled.wrappedBuffer(streamOutput.bytes()));
			}
		} else if ("file_command".equals(type)) {
			FileCommandPacket fileCommandPacket = new FileCommandPacket();
			fileCommandPacket.readFrom(streamInput);

			Command command = fileCommandPacket.getCommand();
			switch (command) {
				case EXIST:
					ConsoleLogger.println("File [" + file.getName() + "] exists to server");
					done.compareAndSet(false, true);
					break;
				case NONE_EXIST:
				case OVERWRITE:
					ChannelFuture sendFuture = ctx.writeAndFlush(
						new EncryptChunkedFile(file, 64 * 1024 * 1024,
							new AesCrypt("aes-256-ofb", password)), ctx.newProgressivePromise());

					final FileInfo fileInfo = fileCommandPacket.getFileInfo();
					sendFuture.addListener(new ChannelProgressiveFutureListener() {
						@Override
						public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) throws Exception{
							if (total < 0) {
								ConsoleLogger.println(future.channel() + " Transfer progress: " + progress
									+ " [" + new SizeValue(progress) + "]");
							} else {
								ConsoleLogger.println(future.channel() + " Transfer progress: " + progress + " [" + new SizeValue(progress) + "] / "
									+ total + " [" + new SizeValue(total)+ "]");
							}
						}

						@Override
						public void operationComplete(ChannelProgressiveFuture future) throws Exception {
							try (StreamOutput streamOutput = new StreamOutput()) {
								streamOutput.writeString("end");

								ctx.writeAndFlush(Unpooled.wrappedBuffer(streamOutput.bytes()));
							}

							done.compareAndSet(false, true);
						}
					});
					break;
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);
	}
}
