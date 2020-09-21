package com.kakaobank.common.crypt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public interface Crypt {

	void encrypt(byte[] data, OutputStream stream) throws IOException;

	void encrypt(byte[] data, int length, OutputStream stream) throws IOException;

	void decrypt(byte[] data, OutputStream stream) throws IOException;

	void decrypt(byte[] data, int length, OutputStream stream) throws IOException;
}
