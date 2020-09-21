package com.kakaobank.server.file;

import com.kakaobank.common.crypt.Crypt;
import com.kakaobank.common.crypt.CryptUtil;
import com.kakaobank.common.io.StreamInput;
import com.kakaobank.common.transport.file.EncryptChunkedFile;
import com.kakaobank.common.unit.SizeValue;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileWriter implements Closeable {

	private final Logger logger = LoggerFactory.getLogger(FileWriter.class);

	private final File file;
	private final Crypt crypt;
	private final long fileSize;
	private long offset;

	private FileOutputStream fos;

	public FileWriter(File file, long fileSize, Crypt crypt) {
		this.file = file;
		this.crypt = crypt;
		this.fileSize = fileSize;
	}

	public void write(ByteBuf byteBuf) throws IOException {
		if (this.fos == null) {
			logger.info("start to write file [{}]", file);
			this.fos = new FileOutputStream(file);
		}

		offset += byteBuf.readableBytes();
		CryptUtil.decrypt(crypt, byteBuf, fos);
		fos.flush();
	}

	public boolean isDone() {
		return offset >= fileSize;
	}

	@Override
	public void close() throws IOException {
		logger.info("done. file [{}], size [{}]", file, new SizeValue(file.length()));

		if (fos != null) {
			fos.close();
		}
	}
}
