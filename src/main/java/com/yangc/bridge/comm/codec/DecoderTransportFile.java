package com.yangc.bridge.comm.codec;

import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;

import com.yangc.bridge.bean.FileBean;
import com.yangc.bridge.comm.protocol.Protocol;

public class DecoderTransportFile implements MessageDecoder {

	private Charset charset;

	public DecoderTransportFile(Charset charset) {
		this.charset = charset;
	}

	@Override
	public MessageDecoderResult decodable(IoSession session, IoBuffer in) {
		if (in.remaining() < 46) {
			return NEED_DATA;
		}
		if (in.get() == Protocol.START_TAG) {
			if (in.get() == 4) {
				in.skip(36);
				short fromLength = in.getShort();
				short toLength = in.getShort();
				int dataLength = in.getInt();
				if (in.limit() >= 46 + fromLength + toLength + 1 + dataLength + 2) {
					if (in.skip(fromLength + toLength).get() == Protocol.END_TAG) {
						byte crc = 0;
						byte[] b = Arrays.copyOfRange(in.array(), 0, 46 + fromLength + toLength + 1 + dataLength);
						for (int i = 0; i < b.length; i++) {
							crc += b[i];
						}
						if (in.skip(dataLength).get() == crc && in.get() == Protocol.FINAL_TAG) {
							return OK;
						}
					}
				} else {
					return NEED_DATA;
				}
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
		int dataLength = in.getInt();
		String from = in.getString(fromLength, this.charset.newDecoder());
		String to = in.getString(toLength, this.charset.newDecoder());
		in.get(); // endTag
		short fileNameLength = in.getShort();
		String fileName = in.getString(fileNameLength, this.charset.newDecoder());
		long fileSize = in.getLong();
		String fileMd5 = in.getString(32, this.charset.newDecoder());
		short offset = in.getShort();
		byte[] data = new byte[dataLength - fileNameLength - 44];
		in.get(data);
		in.get(); // crc
		in.get(); // finalTag

		FileBean file = new FileBean();
		file.setContentType(contentType);
		file.setUuid(uuid);
		file.setFrom(from);
		file.setTo(to);
		file.setFileName(fileName);
		file.setFileSize(fileSize);
		file.setFileMd5(fileMd5);
		file.setOffset(offset);
		file.setData(data);
		out.write(file);
		return OK;
	}

	@Override
	public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
	}

}
