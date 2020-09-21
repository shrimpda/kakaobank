package com.kakaobank.common.packet;

import com.kakaobank.common.file.FileInfo;
import com.kakaobank.common.io.StreamInput;
import com.kakaobank.common.io.StreamOutput;
import java.io.IOException;

public class FileInfoPacket extends TransportPacket {

	private FileInfo fileInfo;

	public FileInfoPacket() {

	}

	public FileInfoPacket(FileInfo fileInfo) {
		this.fileInfo = fileInfo;
	}

	public FileInfo getFileInfo() {
		return fileInfo;
	}

	@Override
	public void readFrom(StreamInput streamInput) throws IOException {
		fileInfo = new FileInfo(streamInput);
	}

	@Override
	public void writeTo(StreamOutput streamOutput) throws IOException {
		fileInfo.writeTo(streamOutput);
	}
}
