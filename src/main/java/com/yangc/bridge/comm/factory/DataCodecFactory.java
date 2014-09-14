package com.yangc.bridge.comm.factory;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import org.apache.commons.lang.StringUtils;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

import com.yangc.bridge.comm.Server;
import com.yangc.bridge.comm.codec.protobuf.ProtobufDecoderData;
import com.yangc.bridge.comm.codec.protobuf.ProtobufEncoderData;
import com.yangc.bridge.comm.codec.prototype.PrototypeDecoderData;
import com.yangc.bridge.comm.codec.prototype.PrototypeEncoderData;

public class DataCodecFactory implements ProtocolCodecFactory {

	@Override
	public ProtocolEncoder getEncoder(IoSession session) throws Exception {
		return StringUtils.equals(Server.CODEC, "prototype") ? new PrototypeEncoderData() : new ProtobufEncoderData();
	}

	@Override
	public ProtocolDecoder getDecoder(IoSession session) throws Exception {
		CharsetDecoder charsetDecoder = Charset.forName(Server.CHARSET_NAME).newDecoder();
		return StringUtils.equals(Server.CODEC, "prototype") ? new PrototypeDecoderData(charsetDecoder) : new ProtobufDecoderData(charsetDecoder);
	}

}
