package com.yangc.bridge.codec;

import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;

import com.yangc.bridge.protocol.Protocol;

public class DataDecoder implements MessageDecoder {

	private Charset charset;

	public DataDecoder(Charset charset) {
		this.charset = charset;
	}

	@Override
	public MessageDecoderResult decodable(IoSession session, IoBuffer in) {
		if (in.remaining() < 6) {
			return NEED_DATA;
		}
		if (in.get() == Protocol.START_TAG) {
			short headLength = in.getShort();
			short bodyLength = in.getShort();
			if (in.get() == Protocol.END_TAG) {
				if (in.limit() >= headLength + bodyLength) {
					if (in.get(36) == 0 && in.get(headLength + bodyLength - 1) == Protocol.FINAL_TAG) {
						return OK;
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
		byte startTag = in.get();
		short headLength = in.getShort();
		short bodyLength = in.getShort();
		byte endTag = in.get();

		byte[] uuid = new byte[36];
		in.get(uuid, 0, uuid.length);

		byte contentType = in.get();
		short fromLength = in.getShort();
		short toLength = in.getShort();

		byte[] from = new byte[fromLength];
		in.get(from, 0, fromLength);
		byte[] to = new byte[toLength];
		in.get(to, 0, toLength);

		return NOT_OK;
	}

	@Override
	public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {

	}

}
