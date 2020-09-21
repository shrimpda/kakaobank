package com.kakaobank.server.transport.handler;

import com.kakaobank.common.io.StreamOutput;
import com.kakaobank.common.packet.IdentityPacket;
import com.kakaobank.common.packet.ServerInfoPacket;
import com.kakaobank.server.configs.Configs;
import com.kakaobank.server.encrypt.Encrypt;
import com.kakaobank.server.node.NodeInfo;
import com.kakaobank.server.transport.TransportService;
import com.kakaobank.server.transport.handler.packet.NodeInfoPacket;
import com.kakaobank.server.transport.netty.NettyChannel;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import java.io.IOException;

public class IdentityPacketHandler extends PacketHandler<IdentityPacket> {

	public static final String NAME = "identity";

	private final Configs configs;
	private final TransportService transportService;
	private final String password;

	public IdentityPacketHandler(Configs configs, TransportService transportService) {
		super(IdentityPacket::new);
		this.configs = configs;
		this.transportService = transportService;
		this.password = Encrypt.PASSWORD.get(configs);
	}

	@Override
	public void execute(IdentityPacket packet, NettyChannel channel) throws IOException {
		switch (packet.getType()) {
			case AGENT:
				ServerInfoPacket serverInfoPacket = new ServerInfoPacket(password);

				try (StreamOutput streamOutput = new StreamOutput()) {
					streamOutput.writeString("server_info");
					serverInfoPacket.writeTo(streamOutput);

					channel.write(streamOutput.bytes());
				}

				break;
			case SERVER:
				break;
		}
	}
}
