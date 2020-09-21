package com.kakaobank.server.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;

public class IOUtil {

	private IOUtil() {

	}

	public static void closeQuietly(Closeable... closeables) {
		closeQuietly(Arrays.asList(closeables));
	}

	public static void closeQuietly(final Iterable<? extends Closeable> closeables) {
		for (final Closeable object : closeables) {
			try {
				if (object != null) {
					object.close();
				}
			} catch (final IOException | RuntimeException e) {

			}
		}
	}
}
