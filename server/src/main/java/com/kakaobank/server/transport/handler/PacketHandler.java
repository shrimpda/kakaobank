package com.kakaobank.server.transport.handler;

import com.kakaobank.common.packet.TransportPacket;
import com.kakaobank.server.transport.netty.NettyChannel;
import io.netty.channel.Channel;
import java.io.IOException;
import java.util.function.Supplier;

public abstract class PacketHandler<T extends TransportPacket> {

	private final Supplier<T> packetFactory;

	public PacketHandler(Supplier<T> packetFactory) {
		this.packetFactory = packetFactory;
	}

	public T createPacket() {
		return packetFactory.get();
	}

	public abstract void execute(T packet, NettyChannel channel) throws IOException;
}
