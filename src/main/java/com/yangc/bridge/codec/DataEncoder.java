package com.yangc.bridge.codec;

import java.io.File;
import java.nio.charset.Charset;
import java.util.UUID;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.yangc.bridge.bean.ProtocolChat;

public class DataEncoder extends ProtocolEncoderAdapter {

	private Charset charset;

	public DataEncoder(Charset charset) {
		this.charset = charset;
	}

	@Override
	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
		if (message instanceof String) {
			String uuid = UUID.randomUUID().toString();
			byte[] data = ((String) message).getBytes(charset);

			ProtocolChat chat = new ProtocolChat();
			chat.setType((byte) 0);
			chat.setUuid(uuid.getBytes());

		} else if (message instanceof File) {

		}
	}

}
