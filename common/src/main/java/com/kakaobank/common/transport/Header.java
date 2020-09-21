package com.kakaobank.common.transport;

public class Header {

	public static final byte[] MAGIC_NUMBER = new byte[] {
		'K', 'A', 'K', 'A', 'O'
	};

	public static final int MAGIC_NUMBER_LENGTH = 5;
	public static final int MESSAGE_LENGTH = 4;

	public static final int HEADER_LENGTH = MAGIC_NUMBER_LENGTH + MESSAGE_LENGTH;
}
