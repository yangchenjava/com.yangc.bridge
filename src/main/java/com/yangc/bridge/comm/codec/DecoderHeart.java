package com.yangc.bridge.comm.codec;

import java.util.Arrays;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;

import com.yangc.bridge.comm.protocol.ContentType;
import com.yangc.bridge.comm.protocol.Protocol;
import com.yangc.bridge.comm.protocol.ProtocolHeart;

public class DecoderHeart implements MessageDecoder {

	@Override
	public MessageDecoderResult decodable(IoSession session, IoBuffer in) {
		if (in.remaining() < 3) {
			return NEED_DATA;
		}
		if (in.get() == Protocol.START_TAG && in.get() == ContentType.HEART && in.get() == Protocol.END_TAG) {
			if (in.limit() >= 5) {
				byte crc = 0;
				byte[] b = Arrays.copyOfRange(in.array(), 0, 3);
				for (int i = 0; i < b.length; i++) {
					crc += b[i];
				}
				if (in.get() == crc && in.get() == Protocol.FINAL_TAG) {
					return OK;
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
		byte contentType = in.get();
		in.get(); // endTag
		in.get(); // crc
		in.get(); // finalTag
		ProtocolHeart protocol = new ProtocolHeart();
		protocol.setContentType(contentType);
		out.write(protocol);
		return OK;
	}

	@Override
	public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
	}

}
