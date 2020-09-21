package com.kakaobank.common.packet;

import com.kakaobank.common.io.StreamInput;
import com.kakaobank.common.io.StreamOutput;
import java.io.IOException;

public class ServerInfoPacket extends TransportPacket {

	private String password;

	public ServerInfoPacket() {

	}

	public ServerInfoPacket(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	@Override
	public void readFrom(StreamInput streamInput) throws IOException {
		password = streamInput.readString();
	}

	@Override
	public void writeTo(StreamOutput streamOutput) throws IOException {
		streamOutput.writeString(password);
	}
}
