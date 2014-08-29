package com.yangc.bridge.comm.codec;

import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;

import com.yangc.bridge.bean.ResultBean;
import com.yangc.bridge.comm.protocol.ContentType;
import com.yangc.bridge.comm.protocol.Protocol;

public class DecoderResult implements MessageDecoder {

	private Charset charset;

	public DecoderResult(Charset charset) {
		this.charset = charset;
	}

	@Override
	public MessageDecoderResult decodable(IoSession session, IoBuffer in) {
		if (in.remaining() < 44) {
			return NEED_DATA;
		}
		if (in.get() == Protocol.START_TAG && in.get() == ContentType.RESULT) {
			in.skip(36);
			short toLength = in.getShort();
			int dataLength = in.getInt();
			if (in.limit() >= 44 + toLength + 1 + dataLength + 2) {
				if (in.skip(toLength).get() == Protocol.END_TAG) {
					byte crc = 0;
					byte[] b = Arrays.copyOfRange(in.array(), 0, 44 + toLength + 1 + dataLength);
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
		return NOT_OK;
	}

	@Override
	public MessageDecoderResult decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		in.get(); // startTag
		in.get(); // contentType
		String uuid = in.getString(36, this.charset.newDecoder());
		short toLength = in.getShort();
		int dataLength = in.getInt();
		String to = in.getString(toLength, this.charset.newDecoder());
		in.get(); // endTag
		byte success = in.get();
		String message = in.getString(dataLength - 1, this.charset.newDecoder());
		in.get(); // crc
		in.get(); // finalTag

		ResultBean result = new ResultBean();
		result.setUuid(uuid);
		result.setTo(to);
		result.setSuccess(success == 0 ? false : true);
		result.setMessage(message);
		out.write(result);
		return OK;
	}

	@Override
	public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
	}

}
