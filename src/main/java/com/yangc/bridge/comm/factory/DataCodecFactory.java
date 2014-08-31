package com.yangc.bridge.comm.factory;

import java.nio.charset.Charset;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

import com.yangc.bridge.comm.Server;
import com.yangc.bridge.comm.codec.DecoderData;
import com.yangc.bridge.comm.codec.EncoderData;

public class DataCodecFactory implements ProtocolCodecFactory {

	@Override
	public ProtocolEncoder getEncoder(IoSession session) throws Exception {
		return new EncoderData();
	}

	@Override
	public ProtocolDecoder getDecoder(IoSession session) throws Exception {
		return new DecoderData(Charset.forName(Server.CHARSET_NAME).newDecoder());
	}

}
