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

	private final ProtocolEncoder encoder;
	private final ProtocolDecoder decoder;

	public DataCodecFactory() {
		if (StringUtils.equals(Server.CODEC, "protobuf")) {
			this.encoder = new ProtobufEncoderData();
		} else if (StringUtils.equals(Server.CODEC, "messagepack")) {
			this.encoder = new MessagePackEncoderData();
		} else {
			this.encoder = new PrototypeEncoderData();
		}

		if (StringUtils.equals(Server.CODEC, "protobuf")) {
			this.decoder = new ProtobufDecoderData();
		} else if (StringUtils.equals(Server.CODEC, "messagepack")) {
			this.decoder = new MessagePackDecoderData();
		} else {
			this.decoder = new PrototypeDecoderData(Charset.forName(Server.CHARSET_NAME).newDecoder());
		}
	}

	@Override
	public ProtocolEncoder getEncoder(IoSession session) throws Exception {
		return this.encoder;
	}

	@Override
	public ProtocolDecoder getDecoder(IoSession session) throws Exception {
		return this.decoder;
	}

}
