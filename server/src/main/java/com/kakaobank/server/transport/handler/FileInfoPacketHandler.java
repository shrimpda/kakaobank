package com.kakaobank.server.transport.handler;

import com.kakaobank.common.crypt.AesCrypt;
import com.kakaobank.common.file.FileInfo;
import com.kakaobank.common.io.StreamOutput;
import com.kakaobank.common.packet.FileCommandPacket;
import com.kakaobank.common.packet.FileCommandPacket.Command;
import com.kakaobank.common.packet.FileInfoPacket;
import com.kakaobank.server.configs.Configs;
import com.kakaobank.server.encrypt.Encrypt;
import com.kakaobank.server.file.FileRepository;
import com.kakaobank.server.file.FileWriter;
import com.kakaobank.server.transport.TransportService;
import com.kakaobank.server.transport.netty.ChunkedFileHandler;
import com.kakaobank.server.transport.netty.NettyChannel;
import com.twmacinta.util.MD5;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileInfoPacketHandler extends PacketHandler<FileInfoPacket> {

	public static final Logger logger = LoggerFactory.getLogger(FileInfo.class);

	public static final String NAME = "file_info";

	private final Configs configs;
	private final TransportService transportService;
	private final FileRepository fileRepository;
	private final String password;

	public FileInfoPacketHandler(Configs configs, TransportService transportService, FileRepository fileRepository) {
		super(FileInfoPacket::new);
		this.configs = configs;
		this.transportService = transportService;
		this.fileRepository = fileRepository;
		this.password = Encrypt.PASSWORD.get(configs);
	}

	@Override
	public void execute(FileInfoPacket packet, NettyChannel channel) throws IOException {
		FileInfo fileInfo = packet.getFileInfo();

		logger.info("Receive file information. name [{}], size [{}], hash [{}]",
			fileInfo.getName(), fileInfo.getSize(), fileInfo.getHash());

		File file = fileRepository.get(fileInfo.getName());

		if (file.exists()) {
			logger.info("File [{}] already exists. check if the hash is the same.", fileInfo.getName());

			try {
				String hash = MD5.asHex(MD5.getHash(file));

				if (hash.equals(fileInfo.getHash())) {
					logger.info("File [{}] hash is the same. receive [{}], exist [{}]",
						fileInfo.getName(), fileInfo.getHash(), hash);

					FileCommandPacket fileCommandPacket = new FileCommandPacket(fileInfo, Command.EXIST);
					try (StreamOutput streamOutput = new StreamOutput()) {
						streamOutput.writeString("file_command");
						fileCommandPacket.writeTo(streamOutput);

						channel.write(streamOutput.bytes());
					}
				} else {
					logger.info("File [{}] hash is not the same. receive [{}], exist [{}]",
						fileInfo.getName(), fileInfo.getHash(), hash);

					FileCommandPacket fileCommandPacket = new FileCommandPacket(fileInfo, Command.OVERWRITE);
					try (StreamOutput streamOutput = new StreamOutput()) {
						streamOutput.writeString("file_command");
						fileCommandPacket.writeTo(streamOutput);

						channel.write(streamOutput.bytes());
					}

					if (file.delete()) {
						logger.info("Delete existing file [{}]", file.getName());
					}

					channel.channel().pipeline().remove("handler");

					FileWriter fileWriter = new FileWriter(file, fileInfo.getSize().getSingles(), new AesCrypt("aes-256-ofb", password));
					channel.channel().pipeline().addLast("handler", new ChunkedFileHandler(transportService, fileWriter));
				}
			} catch (IOException e) {
				logger.error("fail to check hash", e);
			}
		} else {
			logger.info("File [{}] does not exist.", fileInfo.getName());

			FileCommandPacket fileCommandPacket = new FileCommandPacket(fileInfo, Command.NONE_EXIST);
			try (StreamOutput streamOutput = new StreamOutput()) {
				streamOutput.writeString("file_command");
				fileCommandPacket.writeTo(streamOutput);

				channel.write(streamOutput.bytes());
			}

			channel.channel().pipeline().remove("handler");

			FileWriter fileWriter = new FileWriter(file, fileInfo.getSize().getSingles(), new AesCrypt("aes-256-ofb", password));
			channel.channel().pipeline().addLast("handler", new ChunkedFileHandler(transportService, fileWriter));
		}
	}
}
