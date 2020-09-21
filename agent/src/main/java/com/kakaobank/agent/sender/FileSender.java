package com.kakaobank.agent.sender;

import com.kakaobank.agent.console.ConsoleLogger;
import com.kakaobank.common.transport.HeaderEncoder;
import com.kakaobank.common.transport.SizeDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FileRegion;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedStream;
import io.netty.handler.stream.ChunkedWriteHandler;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileSender {

	private final String host;
	private final int port;
	private final File file;

	public FileSender(String host, int port, File file) {
		this.host = host;
		this.port = port;
		this.file = file;
	}

	public void send() {
		AtomicBoolean done = new AtomicBoolean(false);

		EventLoopGroup group = new NioEventLoopGroup();

		try {
			Bootstrap bootsrap = new Bootstrap();
			bootsrap.group(group);
			bootsrap.channel(NioSocketChannel.class);
			bootsrap.option(ChannelOption.SO_KEEPALIVE, false);
			bootsrap.handler(new ChannelInitializer<Channel>() {
				@Override
				protected void initChannel(Channel ch) throws Exception {
					ch.pipeline().addLast(new SizeDecoder());
					ch.pipeline().addLast(new HeaderEncoder());
					ch.pipeline().addLast(new ChunkedWriteHandler());
					ch.pipeline().addLast(new MessageHandler(file, done));
				}
			});

			ChannelFuture channelFuture = bootsrap.connect(host, port);
			Channel channel = channelFuture.channel();
			if (channel == null) {
				throw new IOException(channelFuture.cause());
			}

			try {
				channelFuture.get(1000, TimeUnit.MILLISECONDS);
			} catch (ExecutionException e) {
				e.printStackTrace(ConsoleLogger.getWriter());
			} catch (TimeoutException e) {
				e.printStackTrace(ConsoleLogger.getWriter());
			}

			channel.closeFuture().addListener(f -> {
				done.compareAndSet(false, true);
			});

			while (true) {
				if (done.get()) {
					break;
				}
				Thread.sleep(10);
			}
		} catch (InterruptedException | IOException e) {
			e.printStackTrace(ConsoleLogger.getWriter());
		} finally {
			group.shutdownGracefully();
		}
	}
}
