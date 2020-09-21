package com.kakaobank.common.transport.file;

import com.kakaobank.common.crypt.Crypt;
import com.kakaobank.common.crypt.CryptUtil;
import com.kakaobank.common.io.StreamOutput;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.stream.ChunkedInput;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

public class EncryptChunkedFile implements ChunkedInput<ByteBuf> {
	public static final String TRANSFER = "transfer";
	public static final String END = "end";

	public static final int BUFFER_SIZE = 4096;

	private final RandomAccessFile file;
	private final long startOffset;
	private final long endOffset;
	private final int chunkSize;
	private long offset;
	private final Crypt crypt;

	public EncryptChunkedFile(File file, int chunkSize, Crypt crypt) throws IOException {
		this(new RandomAccessFile(file, "r"), chunkSize, crypt);
	}

	public EncryptChunkedFile(RandomAccessFile file, int chunkSize, Crypt crypt) throws IOException {
		this(file, 0, file.length(), chunkSize, crypt);
	}

	public EncryptChunkedFile(RandomAccessFile file, long offset, long length, int chunkSize, Crypt crypt) throws IOException {
		this.file = file;
		this.offset = startOffset = offset;
		this.endOffset = offset + length;
		this.chunkSize = chunkSize;
		this.crypt = crypt;

		file.seek(offset);
	}

	public long startOffset() {
		return startOffset;
	}

	public long endOffset() {
		return endOffset;
	}

	public long currentOffset() {
		return offset;
	}

	@Override
	public boolean isEndOfInput() throws Exception {
		return !(offset < endOffset && file.getChannel().isOpen());
	}

	@Override
	public void close() throws Exception {
		file.close();
	}

	@Deprecated
	@Override
	public ByteBuf readChunk(ChannelHandlerContext ctx) throws Exception {
		return readChunk(ctx.alloc());
	}

	@Override
	public ByteBuf readChunk(ByteBufAllocator allocator) throws Exception {
		long offset = this.offset;
		if (offset >= endOffset) {
			return null;
		}

		int chunkSize = (int) Math.min(this.chunkSize, endOffset - offset);

		ByteBuf buf = null;
		boolean release = true;
		try {
			byte[] bytes = new byte[chunkSize];
			file.readFully(bytes, 0, chunkSize);
			byte[] encryptBytes = CryptUtil.encrypt(crypt, bytes);

			try (StreamOutput streamOutput = new StreamOutput()) {
				streamOutput.writeString(TRANSFER);
				streamOutput.writeByte(encryptBytes, 0, encryptBytes.length);

				byte[] result = streamOutput.bytes();
				buf = allocator.heapBuffer(result.length);
				buf.writeBytes(result, 0, result.length);
				buf.writerIndex(result.length);
			}

			this.offset = offset + chunkSize;

			release = false;
			encryptBytes = null;
			bytes = null;
			return buf;
		} finally {
			if (release) {
				if (buf != null) {
					buf.release();
				}
			}
		}
	}

	@Override
	public long length() {
		return endOffset - startOffset;
	}

	@Override
	public long progress() {
		return offset - startOffset;
	}
}
