package com.kakaobank.common.io;

import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class StreamInput {

	private final ByteBuf buffer;

	public StreamInput(ByteBuf buffer) {
		this.buffer = buffer;
	}

	public byte read() {
		return buffer.readByte();
	}

	public void read(byte[] buf, int offset, int length) {
		buffer.readBytes(buf, offset, length);
	}

	public int readInt() {
		return buffer.readInt();
	}

	public long readLong() throws IOException {
		return (((long) readInt()) << 32) | (readInt() & 0xFFFFFFFFL);
	}

	public String readString() throws IOException {
		int length = readInt();
		byte[] bytes = new byte[length];
		read(bytes, 0, length);
		try {
			return new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IOException(e);
		} finally {
			bytes = null;
		}
	}

	public <T extends Enum<T>> T readEnum(Class<T> enumClass) throws IOException {
		int ordinal = readInt();
		T[] enums = enumClass.getEnumConstants();
		if (ordinal < 0 || ordinal >= enums.length) {
			throw new IOException("Unknown " + enumClass.getSimpleName() + " ordinal [" + ordinal + "]");
		}
		return enums[ordinal];
	}

	public static int readInt(InputStream inputStream) throws IOException {
		return ((inputStream.read() & 0xFF) << 24) | ((inputStream.read() & 0xFF) << 16)
			| ((inputStream.read() & 0xFF) << 8) | (inputStream.read() & 0xFF);
	}
}
