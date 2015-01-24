package com.yangc.bridge.comm.factory;

import org.apache.commons.lang3.StringUtils;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;

import com.yangc.bridge.comm.Server;
import com.yangc.bridge.comm.protocol.protobuf.ProtobufMessage;
import com.yangc.bridge.comm.protocol.prototype.ProtocolHeart;

public class KeepAliveFactory implements KeepAliveMessageFactory {

	@Override
	public boolean isRequest(IoSession session, Object message) {
		return message instanceof Byte;
	}

	@Override
	public boolean isResponse(IoSession session, Object message) {
		return false;
	}

	@Override
	public Object getRequest(IoSession session) {
		return null;
	}

	@Override
	public Object getResponse(IoSession session, Object request) {
		if (StringUtils.equals(Server.CODEC, "prototype")) {
			return new ProtocolHeart();
		} else {
			return ProtobufMessage.Heart.newBuilder().build();
		}
	}

}
