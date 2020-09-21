package com.kakaobank.common.io;

import java.io.IOException;

public interface Writeable {

	void writeTo(StreamOutput streamOutput) throws IOException;
}
