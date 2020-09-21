package com.kakaobank.common.packet;

import com.kakaobank.common.io.StreamInput;
import com.kakaobank.common.io.Writeable;
import java.io.IOException;

public abstract class TransportPacket implements Writeable {

	public abstract void readFrom(StreamInput streamInput) throws IOException;
}
