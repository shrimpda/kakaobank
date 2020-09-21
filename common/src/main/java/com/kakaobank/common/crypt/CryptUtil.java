package com.kakaobank.common.crypt;

import io.netty.buffer.ByteBuf;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.print.DocFlavor.INPUT_STREAM;

public class CryptUtil {

	public static byte[] encrypt(Crypt crypt, ByteBuf byteBuf) throws IOException {
		byte[] data = null;

		try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			if (!byteBuf.hasArray()) {
				int length = byteBuf.readableBytes();
				byte[] bytes = new byte[length];
				byteBuf.getBytes(byteBuf.readerIndex(), bytes);
				crypt.encrypt(bytes, bytes.length, outputStream);
				data = outputStream.toByteArray();
			}
		}

		return data;
	}

	public static byte[] encrypt(Crypt crypt, byte[] bytes) throws IOException {
		byte[] data = null;

		try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			crypt.encrypt(bytes, bytes.length, outputStream);
			data = outputStream.toByteArray();
		}

		return data;
	}

	public static byte[] decrypt(Crypt crypt, ByteBuf byteBuf) throws IOException {
		byte[] data = null;

		try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			if (!byteBuf.hasArray()) {
				int length = byteBuf.readableBytes();
				byte[] bytes = new byte[length];
				byteBuf.getBytes(0, bytes);
				crypt.decrypt(bytes, bytes.length, outputStream);
				data = outputStream.toByteArray();
			}
		}

		return data;
	}

	public static void decrypt(Crypt crypt, ByteBuf byteBuf, OutputStream stream) throws IOException {
		if (!byteBuf.hasArray()) {
			int length = byteBuf.readableBytes();
			byte[] bytes = new byte[length];
			byteBuf.getBytes(byteBuf.readerIndex(), bytes);
			crypt.decrypt(bytes, bytes.length, stream);
		}
	}

	public static void decrypt(Crypt crypt, InputStream in, int length, OutputStream stream) throws IOException {
		byte[] bytes = new byte[length];
		in.read(bytes);
		crypt.decrypt(bytes, bytes.length, stream);
	}

	public static String getMD5(InputStream is) throws IOException {
		StringBuffer hexString = new StringBuffer();

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");

			byte[] dataBytes = new byte[1024];

			int nread = 0;

			while ((nread = is.read(dataBytes)) != -1) {
				md.update(dataBytes, 0, nread);
			}

			byte[] mdbytes = md.digest();

			StringBuffer sb = new StringBuffer();

			for (int i = 0; i < mdbytes.length; i++) {
				sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
			}

			for (int i=0;i<mdbytes.length;i++) {
				String hex=Integer.toHexString(0xff & mdbytes[i]);

				if(hex.length()==1) {
					hexString.append('0');
				}
				hexString.append(hex);
			}
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		}

		return hexString.toString();
	}
}
