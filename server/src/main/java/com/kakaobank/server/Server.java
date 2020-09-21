package com.kakaobank.server;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.kakaobank.server.configs.Configs;
import com.kakaobank.server.node.Node;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

public class Server {

	private final Thread keepAlive;
	private final CountDownLatch latch = new CountDownLatch(1);

	public Server() {
		keepAlive = new Thread(() -> {
			try {
				latch.await();
			} catch (InterruptedException e) {

			}
		});
		Runtime.getRuntime().addShutdownHook(new Thread(latch::countDown));
	}

	public static void main(String[] args) throws Exception {
		Server application = new Server();

		application.start();
	}

	public void start() throws IOException {
		Path configPath = Paths.get("../config");

		configureLogback(configPath);

		File configFile = configPath.resolve("application.yml").toFile();
		Map<String, Object> configMap = readConfigFile(configFile);

		Configs.Builder builder = Configs.builder().putAll(configMap);
		Configs configs = builder.build();

		Node node = new Node(configs);
		node.start();
		keepAlive.start();
	}

	public static void processParser(JsonParser parser, StringBuilder keyBuilder, Map<String, Object> map) throws IOException {
		final int length = keyBuilder.length();
		while (parser.nextToken() != JsonToken.END_OBJECT) {
			if (parser.currentToken() == JsonToken.FIELD_NAME) {
				keyBuilder.setLength(length);
				keyBuilder.append(parser.getCurrentName());
			} else if (parser.currentToken() == JsonToken.START_OBJECT) {
				keyBuilder.append('.');
				processParser(parser, keyBuilder, map);
			} else if (parser.currentToken() == JsonToken.START_ARRAY) {
				List<String> list = new ArrayList<>();
				while (parser.nextToken() != JsonToken.END_ARRAY) {
					list.add(parser.getText());
				}
				map.put(keyBuilder.toString(), list);
			} else {
				map.put(keyBuilder.toString(), parser.getText());
			}
		}
	}

	public static Map<String, Object> readConfigFile(File file) throws IOException {
		Map<String, Object> configMap = new TreeMap<>();

		YAMLFactory yamlFactory = new YAMLFactory();
		JsonParser parser = yamlFactory.createParser(file);
		if (parser.currentToken() == null) {
			if (parser.nextToken() == null) {
				return configMap;
			}
		}
		StringBuilder keyBuilder = new StringBuilder();
		processParser(parser, keyBuilder, configMap);

		return configMap;
	}

	public void configureLogback(Path configsPath) throws IOException {
		final ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
		if (!(loggerFactory instanceof LoggerContext)) {
			return;
		}

		LoggerContext context = (LoggerContext) loggerFactory;
		context.reset();

		final List<Path> configurations = new ArrayList<>();
		final Set<FileVisitOption> options = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
		Files.walkFileTree(configsPath, options, Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
				throws IOException {
				if (file.getFileName().toString().equals("logback.xml")) {
					configurations.add(file);
				}
				return FileVisitResult.CONTINUE;
			}
		});

		if (configurations.isEmpty()) {
			throw new RuntimeException("no logback.xml found; tried [" + configsPath + "] and its subdirectories");
		}

		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(context);
		try {
			configurator.doConfigure(configurations.get(0).toFile());
		} catch (JoranException e) {
			throw new RuntimeException(
				"found logback.xml; but configuration fail. because of " + e.getMessage());
		}
	}
}
