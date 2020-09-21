package com.kakaobank.server.transport.netty;

import com.kakaobank.common.io.StreamInput;
import com.kakaobank.common.io.StreamOutput;
import com.kakaobank.common.packet.IdentityPacket;
import com.kakaobank.common.packet.TransportPacket;
import com.kakaobank.common.transport.HeaderEncoder;
import com.kakaobank.common.transport.SizeDecoder;
import com.kakaobank.common.type.Type;
import com.kakaobank.server.configs.Config;
import com.kakaobank.server.configs.Configs;
import com.kakaobank.server.node.NodeInfo;
import com.kakaobank.server.transport.Transport;
import com.kakaobank.server.transport.TransportService;
import com.kakaobank.server.transport.handler.NodeInfoPacketHandler;
import com.kakaobank.server.transport.handler.PacketHandler;
import com.kakaobank.server.transport.handler.packet.NodeInfoPacket;
import com.kakaobank.server.util.IOUtil;
import com.kakaobank.server.util.ThreadFactoryMaker;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyTransport implements Transport {

	public static final Logger logger = LoggerFactory.getLogger(NettyTransport.class);

	private final Configs configs;
	private final int workerCount;
	private TransportService transportService;

	private volatile Bootstrap clientBootstrap;

	static final AttributeKey<NettyChannel> CHANNEL_KEY = AttributeKey.newInstance("channel");
	static final AttributeKey<NettyServerChannel> SERVER_CHANNEL_KEY = AttributeKey.newInstance("server-channel");

	public NettyTransport(Configs configs) {
		this.configs = configs;
		workerCount = 10;
	}

	@Override
	public void start() {
		NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(workerCount, ThreadFactoryMaker.create("server_worker"));
		clientBootstrap = createClientBootstrap(eventLoopGroup);
		ServerBootstrap serverBootstrap = createServerBootstrap(eventLoopGroup);
		bind(serverBootstrap);
	}

	public void setTransportService(TransportService transportService) {
		this.transportService = transportService;
	}

	private void bind(ServerBootstrap serverBootstrap) {
		int port = PORT.get(configs);
		String host = HOST.get(configs);
		Channel channel = serverBootstrap.bind(host, port).syncUninterruptibly().channel();

		NettyServerChannel nettyChannel = new NettyServerChannel(channel);
		channel.attr(SERVER_CHANNEL_KEY).set(nettyChannel);

		logger.info("TCP bound to address [{}]", channel.localAddress());
	}

	private ServerBootstrap createServerBootstrap(NioEventLoopGroup eventLoopGroup) {
		ServerBootstrap serverBootstrap = new ServerBootstrap();
		serverBootstrap.group(eventLoopGroup);
		serverBootstrap.channel(NioServerSocketChannel.class);
		serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
		serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
		serverBootstrap.childHandler(new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel ch) throws Exception {
				ch.closeFuture().addListener(f -> {
					if (f.isSuccess() == false) {
						logger.debug("exception while closing channel: "+ ch, f.cause());
					}
				});
				NettyChannel nettyChannel= new NettyChannel(ch, ch.newSucceededFuture());
				ch.attr(CHANNEL_KEY).set(nettyChannel);

				ch.pipeline().addLast("decoder", new SizeDecoder());
				ch.pipeline().addLast("encoder", new HeaderEncoder());
				ch.pipeline().addLast("handler", new NettyHandler(NettyTransport.this));
			}
		});

		return serverBootstrap;
	}

	private Bootstrap createClientBootstrap(EventLoopGroup eventLoopGroup) {
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(eventLoopGroup);
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.option(ChannelOption.TCP_NODELAY, true);
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);

		return bootstrap;
	}

	@Override
	public NettyChannel connectToNode(InetSocketAddress address) throws IOException {
		Bootstrap bootstrap = clientBootstrap.clone();
		bootstrap.remoteAddress(address);
		bootstrap.handler(new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel ch) throws Exception {
				ch.closeFuture().addListener(f -> {
					if (f.isSuccess() == false) {
						logger.debug("exception while closing channel: "+ ch, f.cause());
					}
				});

				ch.pipeline().addLast("decoder", new SizeDecoder());
				ch.pipeline().addLast("encoder", new HeaderEncoder());
				ch.pipeline().addLast("handler", new NettyHandler(NettyTransport.this));
			}
		});
		ChannelFuture future = bootstrap.connect();
		Channel channel = future.channel();
		if (channel == null) {
			throw new IOException(future.cause());
		}

		NettyChannel nettyChannel = new NettyChannel(channel, future);
		channel.attr(CHANNEL_KEY).set(nettyChannel);

		nettyChannel.addConnectListener(future1 -> {
			if (future1.isSuccess()) {
				NodeInfo localNode = transportService.getLocalNode();
				NodeInfoPacket nodeInfoPacket = new NodeInfoPacket(localNode);

				try (StreamOutput streamOutput = new StreamOutput()) {
					streamOutput.writeString(NodeInfoPacketHandler.NAME);
					nodeInfoPacket.writeTo(streamOutput);

					nettyChannel.write(streamOutput.bytes());
				}
			}
		});

		return nettyChannel;
	}

	@Override
	public void messageReceived(NettyChannel channel, StreamInput streamInput) throws IOException {
		String type = streamInput.readString();

		PacketHandler packetHandler = transportService.getPacketHandler(type);
		TransportPacket packet = packetHandler.createPacket();
		packet.readFrom(streamInput);

		packetHandler.execute(packet, channel);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		NettyChannel channel = ctx.channel().attr(CHANNEL_KEY).get();

		logger.warn("exception caught on transport layer [" + channel.channel() + "], closing connection", cause  );
		IOUtil.closeQuietly(channel);
	}

	public interface Connection {

		Channel channel();
	}
}
