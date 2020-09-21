package com.kakaobank.common.crypt;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.SecretKey;

public class Key implements SecretKey {

	private final static int KEY_LENGTH = 32;
	private byte[] key;
	private int length;

	public Key(String password, int length) {
		this.length = length;
		this.key = init(password);
	}

	private byte[] init(String password) {
		MessageDigest md = null;
		byte[] keys = new byte[KEY_LENGTH];
		byte[] temp = null;
		byte[] hash = null;
		byte[] passwordBytes = null;
		int i = 0;

		try {
			md = MessageDigest.getInstance("MD5");
			passwordBytes = password.getBytes("UTF-8");
		} catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}

		while (i < keys.length) {
			if (i == 0) {
				hash = md.digest(passwordBytes);
				temp = new byte[passwordBytes.length + hash.length];
			} else {
				System.arraycopy(hash, 0, temp, 0, hash.length);
				System.arraycopy(passwordBytes, 0, temp, hash.length, passwordBytes.length);
				hash = md.digest(temp);
			}
			System.arraycopy(hash, 0, keys, i, hash.length);
			i += hash.length;
		}

		if (length != KEY_LENGTH) {
			byte[] keysl = new byte[length];
			System.arraycopy(keys, 0, keysl, 0, length);
			return keysl;
		}
		return keys;
	}

	@Override
	public String getAlgorithm() {
		return null;
	}

	@Override
	public String getFormat() {
		return "RAW";
	}

	@Override
	public byte[] getEncoded() {
		return key;
	}
}
