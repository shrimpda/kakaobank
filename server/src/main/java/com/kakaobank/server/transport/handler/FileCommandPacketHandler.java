package com.kakaobank.server.transport.handler;

import com.kakaobank.common.file.FileInfo;
import com.kakaobank.common.packet.FileCommandPacket;
import com.kakaobank.common.packet.FileCommandPacket.Command;
import com.kakaobank.server.file.FileRepository;
import com.kakaobank.server.transport.netty.NettyChannel;
import com.twmacinta.util.MD5;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileCommandPacketHandler extends PacketHandler<FileCommandPacket> {

	public static final String NAME = "file_command";

	public static final Logger logger = LoggerFactory.getLogger(FileCommandPacketHandler.class);

	private final FileRepository fileRepository;

	public FileCommandPacketHandler(FileRepository fileRepository) {
		super(FileCommandPacket::new);
		this.fileRepository = fileRepository;
	}

	@Override
	public void execute(FileCommandPacket packet, NettyChannel channel) throws IOException {
		FileInfo fileInfo = packet.getFileInfo();
		Command command = packet.getCommand();
		switch (command) {
			case END:
				logger.info("File [{}] transfer done.", packet.getFileInfo().getName());
				File file = fileRepository.get(packet.getFileInfo().getName());

				String hash = MD5.asHex(MD5.getHash(file));

				logger.info("File [{}] hash is the same. receive [{}], exist [{}]",
					fileInfo.getName(), fileInfo.getHash(), hash);

				break;
		}
	}
}
