package com.yangc.bridge.comm.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;

import com.yangc.bridge.comm.protocol.Protocol;
import com.yangc.bridge.comm.protocol.ProtocolHeart;

public class DecoderHeart implements MessageDecoder {

	@Override
	public MessageDecoderResult decodable(IoSession session, IoBuffer in) {
		if (in.remaining() >= 3 && in.get() == Protocol.START_TAG && in.get() == 99 && in.get() == Protocol.END_TAG) {
			return OK;
		}
		return NOT_OK;
	}

	@Override
	public MessageDecoderResult decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		ProtocolHeart protocol = new ProtocolHeart();
		protocol.setContentType((byte) 99);
		out.write(protocol);
		return OK;
	}

	@Override
	public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
	}

}
