package com.kakaobank.common.packet;

import com.kakaobank.common.file.FileInfo;
import com.kakaobank.common.io.StreamInput;
import com.kakaobank.common.io.StreamOutput;
import java.io.IOException;

public class FileCommandPacket extends TransportPacket {

	private FileInfo fileInfo;
	private Command command;

	public FileCommandPacket() {

	}

	public FileCommandPacket(FileInfo fileInfo, Command command) {
		this.fileInfo = fileInfo;
		this.command = command;
	}

	public FileInfo getFileInfo() {
		return fileInfo;
	}

	public Command getCommand() {
		return command;
	}

	@Override
	public void readFrom(StreamInput streamInput) throws IOException {
		fileInfo = new FileInfo(streamInput);
		this.command = streamInput.readEnum(Command.class);
	}

	@Override
	public void writeTo(StreamOutput streamOutput) throws IOException {
		fileInfo.writeTo(streamOutput);
		streamOutput.writeEnum(command);
	}

	public enum Command {
		EXIST,
		NONE_EXIST,
		OVERWRITE,
		END
	}
}
