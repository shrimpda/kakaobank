package com.kakaobank.common.crypt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.crypto.StreamBlockCipher;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CFBBlockCipher;
import org.bouncycastle.crypto.modes.OFBBlockCipher;

public class AesCrypt extends BaseCrypt {

	public final static String CIPHER_AES_128_CFB = "aes-128-cfb";
	public final static String CIPHER_AES_192_CFB = "aes-192-cfb";
	public final static String CIPHER_AES_256_CFB = "aes-256-cfb";
	public final static String CIPHER_AES_128_OFB = "aes-128-ofb";
	public final static String CIPHER_AES_192_OFB = "aes-192-ofb";
	public final static String CIPHER_AES_256_OFB = "aes-256-ofb";

	public static Map<String, String> getCiphers() {
		Map<String, String> ciphers = new HashMap<>();
		ciphers.put(CIPHER_AES_128_CFB, AesCrypt.class.getName());
		ciphers.put(CIPHER_AES_192_CFB, AesCrypt.class.getName());
		ciphers.put(CIPHER_AES_256_CFB, AesCrypt.class.getName());
		ciphers.put(CIPHER_AES_128_OFB, AesCrypt.class.getName());
		ciphers.put(CIPHER_AES_192_OFB, AesCrypt.class.getName());
		ciphers.put(CIPHER_AES_256_OFB, AesCrypt.class.getName());

		return ciphers;
	}

	public AesCrypt(String name, String password) {
		super(name, password);
	}

	@Override
	public int getKeyLength() {
		if (CIPHER_AES_128_CFB.equals(name)
			|| CIPHER_AES_128_OFB.equals(name)) {
			return 16;
		} else if (CIPHER_AES_192_CFB.equals(name)
			|| CIPHER_AES_192_OFB.equals(name)) {
			return 24;
		} else if (CIPHER_AES_256_CFB.equals(name)
			|| CIPHER_AES_256_OFB.equals(name)) {
			return 32;
		}

		return 0;
	}

	@Override
	protected StreamBlockCipher getCipher(boolean isEncrypted)
		throws InvalidAlgorithmParameterException {
		AESEngine engine = new AESEngine();
		StreamBlockCipher cipher;

		if (CIPHER_AES_128_CFB.equals(name)) {
			cipher = new CFBBlockCipher(engine, getIVLength() * 8);
		} else if (CIPHER_AES_192_CFB.equals(name)) {
			cipher = new CFBBlockCipher(engine, getIVLength() * 8);
		} else if (CIPHER_AES_256_CFB.equals(name)) {
			cipher = new CFBBlockCipher(engine, getIVLength() * 8);
		} else if (CIPHER_AES_128_OFB.equals(name)) {
			cipher = new OFBBlockCipher(engine, getIVLength() * 8);
		} else if (CIPHER_AES_192_OFB.equals(name)) {
			cipher = new OFBBlockCipher(engine, getIVLength() * 8);
		} else if (CIPHER_AES_256_OFB.equals(name)) {
			cipher = new OFBBlockCipher(engine, getIVLength() * 8);
		} else {
			throw new InvalidAlgorithmParameterException(name);
		}

		return cipher;
	}

	@Override
	public int getIVLength() {
		return 16;
	}

	@Override
	protected SecretKey getKey() {
		return new SecretKeySpec(myKey.getEncoded(), "AES");
	}

	@Override
	protected void _encrypt(byte[] data, OutputStream stream) throws IOException {
		byte[] buffer = new byte[data.length];
		int noBytesProcessed = encryptCipher.processBytes(data, 0, data.length, buffer, 0);
		stream.write(buffer, 0, noBytesProcessed);
	}

	@Override
	protected void _decrypt(byte[] data, OutputStream stream) throws IOException {
		byte[] buffer = new byte[data.length];
		int noBytesProcessed = decryptCipher.processBytes(data, 0, data.length, buffer, 0);
		stream.write(buffer, 0, noBytesProcessed);
	}
}
