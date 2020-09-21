package com.kakaobank.agent;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.kakaobank.agent.console.ConsoleLogger;
import com.kakaobank.agent.sender.FileSender;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.util.Arrays;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.util.PathConverter;
import org.slf4j.LoggerFactory;

public class Agent {

	protected final OptionParser parser = new OptionParser();
	private final OptionSpec<Void> helpOption;
	private final OptionSpec<Path> pathOption;
	private final OptionSpec<String> hostOption;

	Agent() {
		helpOption = parser.acceptsAll(Arrays.asList("h", "help"), "show help").forHelp();
		pathOption = parser.acceptsAll(Arrays.asList("p", "path"),
			"file path to transfer")
			.withRequiredArg()
			.withValuesConvertedBy(new PathConverter());
		hostOption = parser.acceptsAll(Arrays.asList("h", "host"),
			"host to transfer (ex, 1.2.3.4:5)")
			.withRequiredArg();
	}

	private static void offLogger() {
		final LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
		final ch.qos.logback.classic.Logger logbackLogger = ctx.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		logbackLogger.setLevel(Level.OFF);
	}

	public static void main(String[] args) throws Exception {
		offLogger();

		Agent agent = new Agent();
		agent.start(args);
	}

	public void start(String[] args) throws IOException {
		try {
			execute(args);
		} catch (OptionException e) {
			printHelp();
			ConsoleLogger.println("error: " + e.getMessage());
		}
	}

	private void execute(String[] args) throws IOException {
		final OptionSet options = parser.parse(args);

		if (!validateOptions(options)) {
			return;
		}

		Path filePath = pathOption.value(options);
		File file = filePath.toFile();
		if (!file.exists()) {
			ConsoleLogger.println("file [" + file + "] is not exist");
			return;
		}

		ConsoleLogger.println("find file [" + file.toString() + "]");

		String host = hostOption.value(options);

		String[] hostInfo = host.split(":");
		if (hostInfo.length != 2) {
			ConsoleLogger.println("wrong host [" + host + "]");
		}

		FileSender fileSender = new FileSender(hostInfo[0], Integer.parseInt(hostInfo[1]), file);
		fileSender.send();
	}

	private boolean validateOptions(OptionSet optionSet) throws IOException {
		boolean success = true;

		if (optionSet.has(helpOption)) {
			success = false;
		}

		if (!optionSet.has(pathOption)) {
			ConsoleLogger.println("Missing required option(s) " + pathOption);

			success = false;
		}

		if (!optionSet.has(hostOption)) {
			ConsoleLogger.println("Missing required option(s) " + hostOption);

			success = false;
		}

		if (!success) {
			printHelp();
		}

		return success;
	}

	private void printHelp() throws IOException {
		parser.printHelpOn(ConsoleLogger.getWriter());
	}
}
