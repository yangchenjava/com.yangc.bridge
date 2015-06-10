package com.yangc.bridge.comm.factory;

import org.apache.commons.lang3.StringUtils;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;

import com.yangc.bridge.comm.Server;
import com.yangc.bridge.comm.protocol.protobuf.ProtobufMessage;
import com.yangc.bridge.comm.protocol.prototype.ProtocolHeart;

public class KeepAliveFactory implements KeepAliveMessageFactory {

	/**
	 * @功能: 是否为客户端的请求消息
	 * @作者: yangc
	 * @创建日期: 2015年6月9日 下午10:41:39
	 * @param session
	 * @param message
	 * @return
	 * @see org.apache.mina.filter.keepalive.KeepAliveMessageFactory#isRequest(org.apache.mina.core.session.IoSession, java.lang.Object)
	 */
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

	/**
	 * @功能: 返回发给客户端的响应消息
	 * @作者: yangc
	 * @创建日期: 2015年6月9日 下午10:42:20
	 * @param session
	 * @param request
	 * @return
	 * @see org.apache.mina.filter.keepalive.KeepAliveMessageFactory#getResponse(org.apache.mina.core.session.IoSession, java.lang.Object)
	 */
	@Override
	public Object getResponse(IoSession session, Object request) {
		if (StringUtils.equals(Server.CODEC, "prototype")) {
			return new ProtocolHeart();
		} else {
			return ProtobufMessage.Heart.newBuilder().build();
		}
	}

}
