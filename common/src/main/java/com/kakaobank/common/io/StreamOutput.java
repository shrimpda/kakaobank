package com.kakaobank.common.io;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

public class StreamOutput implements AutoCloseable {

	private final DataOutputStream dos;
	private ByteArrayOutputStream baos;

	public StreamOutput() {
		this.baos = new ByteArrayOutputStream();
		this.dos = new DataOutputStream(baos);
	}

	public StreamOutput(ByteArrayOutputStream baos) {
		this.baos = baos;
		this.dos = new DataOutputStream(baos);
	}

	public StreamOutput(BufferedOutputStream bos) {
		this.dos = new DataOutputStream(bos);
	}

	public void writeByte(byte b) throws IOException {
		dos.writeByte(b);
	}

	public void writeByte(byte[] b) throws IOException {
		writeByte(b, 0, b.length);
	}

	public void writeByte(byte[] b, int offset, int length) throws IOException {
		dos.write(b, offset, length);
	}

	public void writeInt(int value) throws IOException {
		dos.writeInt(value);
	}

	public void writeLong(long value) throws IOException {
		dos.writeLong(value);
	}

	public void writeString(String value) throws IOException {
		byte[] bytes = value.getBytes("UTF-8");
		writeInt(bytes.length);
		writeByte(bytes);
	}

	public <T extends Enum<T>> void writeEnum(T enumValue) throws IOException {
		writeInt(enumValue.ordinal());
	}

	public byte[] bytes() {
		return baos.toByteArray();
	}

	@Override
	public void close() throws IOException {
		if (baos != null) {
			baos.close();
		}
		dos.close();
	}
}
