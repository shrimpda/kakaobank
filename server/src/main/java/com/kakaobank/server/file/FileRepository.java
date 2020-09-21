package com.kakaobank.server.file;

import com.kakaobank.server.configs.Config;
import com.kakaobank.server.configs.Configs;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileRepository {

	public static final Config<String> SOURCE_PATH = new Config<>("file.repository.path", configs -> "/files",
		Function.identity());

	private static final Logger logger = LoggerFactory.getLogger(FileRepository.class);

	private final Configs configs;
	private final Path sourcePath;

	public FileRepository(Configs configs) {
		this.configs = configs;
		this.sourcePath = Paths.get(SOURCE_PATH.get(configs));

		logger.info("file repository [{}]", sourcePath);
	}

	public boolean exists(String name) {
		Path filePath = sourcePath.resolve(name);
		return Files.exists(filePath);
	}

	public File get(String name) {
		Path filePath = sourcePath.resolve(name);
		return filePath.toFile();
	}
}
