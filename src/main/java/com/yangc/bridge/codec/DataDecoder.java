package com.yangc.bridge.codec;

import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderAdapter;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public class DataDecoder extends ProtocolDecoderAdapter {

	private Charset charset;

	public DataDecoder(Charset charset) {
		this.charset = charset;
	}

	@Override
	public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {

	}

}
