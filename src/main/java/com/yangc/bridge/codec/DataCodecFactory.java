package com.yangc.bridge.codec;

import java.nio.charset.Charset;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class DataCodecFactory implements ProtocolCodecFactory {

	private String charsetName = "UTF-8";

	public DataCodecFactory() {
	}

	public DataCodecFactory(String charsetName) {
		this.charsetName = charsetName;
	}

	@Override
	public ProtocolEncoder getEncoder(IoSession session) throws Exception {
		return new DataEncoder(Charset.forName(charsetName));
	}

	@Override
	public ProtocolDecoder getDecoder(IoSession session) throws Exception {
		return new DataDecoder(Charset.forName(charsetName));
	}

}
