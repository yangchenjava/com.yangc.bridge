package com.yangc.bridge.comm.factory;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;

import com.yangc.bridge.comm.protocol.Protocol;

public class KeepAliveFactory implements KeepAliveMessageFactory {

	/**
	 * @功能: 0x68 [contentType(99)] 0x68
	 * @作者: yangc
	 * @创建日期: 2014年8月27日 下午9:50:57
	 * @return
	 */
	@Override
	public boolean isRequest(IoSession session, Object message) {
		if (message instanceof IoBuffer) {
			IoBuffer in = (IoBuffer) message;
			if (in.remaining() >= 3 && in.get() == Protocol.START_TAG && in.get() == 99 && in.get() == Protocol.END_TAG) {
				return true;
			}
		}
		return false;
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
		return request;
	}

}
