package com.kakaobank.common.crypt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.SecureRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.crypto.SecretKey;
import org.bouncycastle.crypto.StreamBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

public abstract class BaseCrypt implements Crypt {

	protected final String name;
	protected final SecretKey key;
	protected final Key myKey;
	protected final int ivLength;
	protected final int keyLength;
	protected boolean encryptIVSet;
	protected boolean decryptIVSet;
	protected byte[] encryptIV;
	protected byte[] decryptIV;
	protected final Lock encryptLock = new ReentrantLock();
	protected final Lock decryptLock = new ReentrantLock();
	protected StreamBlockCipher encryptCipher;
	protected StreamBlockCipher decryptCipher;

	public BaseCrypt(String name, String password) {
		this.name = name.toLowerCase();
		ivLength = getIVLength();
		keyLength = getKeyLength();
		myKey = new Key(password, keyLength);
		key = getKey();
	}

	protected void setIV(byte[] iv, boolean isEncrypt) {
		if (ivLength == 0) {
			return;
		}

		if (isEncrypt) {
			encryptIV = new byte[ivLength];
			System.arraycopy(iv, 0, encryptIV, 0, ivLength);
			try {
				encryptCipher = getCipher(isEncrypt);
				ParametersWithIV parameterIV = new ParametersWithIV(new KeyParameter(key.getEncoded()), encryptIV);
				encryptCipher.init(isEncrypt, parameterIV);
			} catch (InvalidAlgorithmParameterException e) {
				e.printStackTrace();
			}
		} else {
			decryptIV = new byte[ivLength];
			System.arraycopy(iv, 0, decryptIV, 0, ivLength);
			try {
				decryptCipher = getCipher(isEncrypt);
				ParametersWithIV parameterIV = new ParametersWithIV(new KeyParameter(key.getEncoded()), decryptIV);
				decryptCipher.init(isEncrypt, parameterIV);
			} catch (InvalidAlgorithmParameterException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void encrypt(byte[] data, OutputStream stream) throws IOException {
		synchronized (encryptLock) {
			if (!encryptIVSet) {
				encryptIVSet = true;
				byte[] iv = randomBytes(ivLength);
				setIV(iv, true);
				stream.write(iv);
			}

			_encrypt(data, stream);
		}
	}

	@Override
	public void encrypt(byte[] data, int length, OutputStream stream) throws IOException {
		byte[] d = new byte[length];
		System.arraycopy(data, 0, d, 0, length);
		encrypt(d, stream);
	}

	@Override
	public void decrypt(byte[] data, OutputStream stream) throws IOException {
		byte[] temp;
		synchronized (decryptLock) {
			if (!decryptIVSet) {
				decryptIVSet = true;
				setIV(data, false);
				temp = new byte[data.length - ivLength];
				System.arraycopy(data, ivLength, temp, 0, data.length
					- ivLength);
			} else {
				temp = data;
			}

			_decrypt(temp, stream);
		}
	}

	@Override
	public void decrypt(byte[] data, int length, OutputStream stream) throws IOException {
		byte[] d = new byte[length];
		System.arraycopy(data, 0, d, 0, length);
		decrypt(d, stream);
	}

	private byte[] randomBytes(int size) {
		byte[] bytes = new byte[size];
		new SecureRandom().nextBytes(bytes);
		return bytes;
	}

	protected abstract StreamBlockCipher getCipher(boolean isEncrypted)
		throws InvalidAlgorithmParameterException;

	protected abstract SecretKey getKey();

	protected abstract void _encrypt(byte[] data, OutputStream stream) throws IOException;

	protected abstract void _decrypt(byte[] data, OutputStream stream) throws IOException;

	protected abstract int getIVLength();

	protected abstract int getKeyLength();
}
