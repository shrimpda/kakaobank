package com.kakaobank.common.file;

import com.kakaobank.common.io.StreamInput;
import com.kakaobank.common.io.StreamOutput;
import com.kakaobank.common.io.Writeable;
import com.kakaobank.common.unit.SizeValue;
import java.io.IOException;

public class FileInfo implements Writeable {

	private String name;
	private SizeValue size;
	private String hash;

	public FileInfo(String name, long size, String hash) {
		this.name = name;
		this.size = new SizeValue(size);
		this.hash = hash;
	}

	public FileInfo(StreamInput streamInput) throws IOException {
		this.name = streamInput.readString();
		this.size = new SizeValue(streamInput.readLong());
		this.hash = streamInput.readString();
	}

	public String getName() {
		return name;
	}

	public SizeValue getSize() {
		return size;
	}

	public String getHash() {
		return hash;
	}

	@Override
	public void writeTo(StreamOutput streamOutput) throws IOException {
		streamOutput.writeString(name);
		size.writeTo(streamOutput);
		streamOutput.writeString(hash);
	}
}
