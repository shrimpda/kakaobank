package com.kakaobank.agent.console;

import java.io.PrintWriter;

public class ConsoleLogger {
	private static final PrintWriter WRITER = System.console() != null ? System.console().writer() : new PrintWriter(System.out);

	private static final String LINE_SEPARATOR = System.lineSeparator();

	private ConsoleLogger() {

	}

	public static void println(String msg) {
		print(msg + LINE_SEPARATOR);
	}

	public static void print(String msg) {
		WRITER.print(msg);
		WRITER.flush();
	}

	public static PrintWriter getWriter() {
		return WRITER;
	}
}
