package com.yangc.bridge.comm.codec;

import java.util.Arrays;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.MessageEncoder;

import com.yangc.bridge.comm.protocol.Protocol;
import com.yangc.bridge.comm.protocol.ProtocolChat;
import com.yangc.bridge.comm.protocol.ProtocolFile;
import com.yangc.bridge.comm.protocol.ProtocolLogin;
import com.yangc.bridge.comm.protocol.ProtocolResult;

public class EncoderData implements MessageEncoder<Protocol> {

	private static final int CAPACITY = 4096;

	@Override
	public void encode(IoSession session, Protocol message, ProtocolEncoderOutput out) throws Exception {
		IoBuffer buffer = IoBuffer.allocate(CAPACITY).setAutoExpand(true);

		if (message instanceof ProtocolResult) {
			this.encodeResult(buffer, (ProtocolResult) message);
		} else if (message instanceof ProtocolLogin) {
			this.encodeLogin(buffer, (ProtocolLogin) message);
		} else if (message instanceof ProtocolChat) {
			this.encodeChat(buffer, (ProtocolChat) message);
		} else if (message instanceof ProtocolFile) {
			this.encodeFile(buffer, (ProtocolFile) message);
		}

		byte crc = 0;
		byte[] b = Arrays.copyOfRange(buffer.array(), 0, buffer.position());
		for (int i = 0; i < b.length; i++) {
			crc += b[i];
		}
		buffer.put(crc);
		buffer.put(Protocol.FINAL_TAG);

		buffer.flip();
		out.write(buffer);
	}

	private void encodeResult(IoBuffer buffer, ProtocolResult protocol) {
		buffer.put(Protocol.START_TAG);
		buffer.put(protocol.getContentType());
		buffer.put(protocol.getUuid());
		buffer.putShort(protocol.getToLength());
		buffer.putShort(protocol.getDataLength());
		buffer.put(protocol.getTo());
		buffer.put(Protocol.END_TAG);
		buffer.put(protocol.getSuccess());
		buffer.put(protocol.getMessage());
	}

	private void encodeLogin(IoBuffer buffer, ProtocolLogin protocol) {
		buffer.put(Protocol.START_TAG);
		buffer.put(protocol.getContentType());
		buffer.put(protocol.getUuid());
		buffer.put(Protocol.END_TAG);
		buffer.putShort(protocol.getUsernameLength());
		buffer.putShort(protocol.getPasswordLength());
		buffer.put(protocol.getUsername());
		buffer.put(protocol.getPassword());
	}

	private void encodeChat(IoBuffer buffer, ProtocolChat protocol) {
		buffer.put(Protocol.START_TAG);
		buffer.put(protocol.getContentType());
		buffer.put(protocol.getUuid());
		buffer.putShort(protocol.getFromLength());
		buffer.putShort(protocol.getToLength());
		buffer.putShort(protocol.getDataLength());
		buffer.put(protocol.getFrom());
		buffer.put(protocol.getTo());
		buffer.put(Protocol.END_TAG);
		buffer.put(protocol.getData());
	}

	private void encodeFile(IoBuffer buffer, ProtocolFile protocol) {
		buffer.put(Protocol.START_TAG);
		buffer.put(protocol.getContentType());
		buffer.put(protocol.getUuid());
		buffer.putShort(protocol.getFromLength());
		buffer.putShort(protocol.getToLength());
		buffer.putShort(protocol.getDataLength());
		buffer.put(protocol.getFrom());
		buffer.put(protocol.getTo());
		buffer.put(Protocol.END_TAG);
		buffer.putShort(protocol.getFileNameLength());
		buffer.put(protocol.getFileName());
		buffer.putLong(protocol.getFileSize());
		buffer.put(protocol.getFileMd5());
		buffer.putShort(protocol.getOffset());
		buffer.put(protocol.getData());
	}

}
