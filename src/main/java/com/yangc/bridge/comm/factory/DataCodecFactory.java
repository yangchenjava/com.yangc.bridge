package com.yangc.bridge.comm.factory;

import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

import com.yangc.bridge.comm.Server;
import com.yangc.bridge.comm.codec.messagepack.MessagePackDecoderData;
import com.yangc.bridge.comm.codec.messagepack.MessagePackEncoderData;
import com.yangc.bridge.comm.codec.protobuf.ProtobufDecoderData;
import com.yangc.bridge.comm.codec.protobuf.ProtobufEncoderData;
import com.yangc.bridge.comm.codec.prototype.PrototypeDecoderData;
import com.yangc.bridge.comm.codec.prototype.PrototypeEncoderData;

public class DataCodecFactory implements ProtocolCodecFactory {

	@Override
	public ProtocolEncoder getEncoder(IoSession session) throws Exception {
		if (StringUtils.equals(Server.CODEC, "protobuf")) {
			return new ProtobufEncoderData();
		}
		if (StringUtils.equals(Server.CODEC, "messagepack")) {
			return new MessagePackEncoderData();
		}
		return new PrototypeEncoderData();
	}

	@Override
	public ProtocolDecoder getDecoder(IoSession session) throws Exception {
		if (StringUtils.equals(Server.CODEC, "protobuf")) {
			return new ProtobufDecoderData();
		}
		if (StringUtils.equals(Server.CODEC, "messagepack")) {
			return new MessagePackDecoderData();
		}
		return new PrototypeDecoderData(Charset.forName(Server.CHARSET_NAME).newDecoder());
	}

}
