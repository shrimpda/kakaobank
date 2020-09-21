package com.kakaobank.common.packet;

import com.kakaobank.common.io.StreamInput;
import com.kakaobank.common.io.StreamOutput;
import com.kakaobank.common.type.Type;
import java.io.IOException;

public class IdentityPacket extends TransportPacket {

	private Type type;

	public IdentityPacket() {

	}

	public IdentityPacket(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	@Override
	public void readFrom(StreamInput streamInput) throws IOException {
		this.type = streamInput.readEnum(Type.class);
	}

	@Override
	public void writeTo(StreamOutput output) throws IOException {
		output.writeEnum(type);
	}
}
