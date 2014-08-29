package com.yangc.bridge.comm.codec;

import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;

import com.yangc.bridge.bean.FileBean;
import com.yangc.bridge.comm.protocol.ContentType;
import com.yangc.bridge.comm.protocol.Protocol;

public class DecoderReadyFile implements MessageDecoder {

	private Charset charset;

	public DecoderReadyFile(Charset charset) {
		this.charset = charset;
	}

	@Override
	public MessageDecoderResult decodable(IoSession session, IoBuffer in) {
		if (in.remaining() < 42) {
			return NEED_DATA;
		}
		if (in.get() == Protocol.START_TAG && in.get() == ContentType.READY_FILE) {
			in.skip(36);
			short fromLength = in.getShort();
			short toLength = in.getShort();
			if (in.limit() >= 42 + fromLength + toLength + 3) {
				if (in.skip(fromLength + toLength).get() == Protocol.END_TAG) {
					short fileNameLength = in.getShort();
					if (in.limit() >= 42 + fromLength + toLength + 3 + fileNameLength + 10) {
						byte crc = 0;
						byte[] b = Arrays.copyOfRange(in.array(), 0, 42 + fromLength + toLength + 3 + fileNameLength + 8);
						for (int i = 0; i < b.length; i++) {
							crc += b[i];
						}
						if (in.skip(fileNameLength + 8).get() == crc && in.get() == Protocol.FINAL_TAG) {
							return OK;
						}
					} else {
						return NEED_DATA;
					}
				}
			} else {
				return NEED_DATA;
			}
		}
		return NOT_OK;
	}

	@Override
	public MessageDecoderResult decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		in.get(); // startTag
		byte contentType = in.get(); // contentType
		String uuid = in.getString(36, this.charset.newDecoder());
		short fromLength = in.getShort();
		short toLength = in.getShort();
		String from = in.getString(fromLength, this.charset.newDecoder());
		String to = in.getString(toLength, this.charset.newDecoder());
		in.get(); // endTag
		short fileNameLength = in.getShort();
		String fileName = in.getString(fileNameLength, this.charset.newDecoder());
		long fileSize = in.getLong();
		in.get(); // crc
		in.get(); // finalTag

		FileBean file = new FileBean();
		file.setContentType(contentType);
		file.setUuid(uuid);
		file.setFrom(from);
		file.setTo(to);
		file.setFileName(fileName);
		file.setFileSize(fileSize);
		out.write(file);
		return OK;
	}

	@Override
	public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
	}

}
