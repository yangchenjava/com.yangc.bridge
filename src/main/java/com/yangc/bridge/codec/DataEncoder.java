package com.yangc.bridge.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.MessageEncoder;

import com.yangc.bridge.protocol.Protocol;
import com.yangc.bridge.protocol.ProtocolChat;
import com.yangc.bridge.protocol.ProtocolFile;

public class DataEncoder implements MessageEncoder<Protocol> {

	private static final int CAPACITY = 4096;

	@Override
	public void encode(IoSession session, Protocol message, ProtocolEncoderOutput out) throws Exception {
		IoBuffer buffer = IoBuffer.allocate(CAPACITY).setAutoExpand(true);

		buffer.put(Protocol.START_TAG);
		buffer.putShort(message.getHeadLength());
		buffer.putShort(message.getBodyLength());
		buffer.put(Protocol.END_TAG);
		buffer.put(message.getUuid());
		buffer.put(message.getContentType());
		buffer.putShort(message.getFromLength());
		buffer.putShort(message.getToLength());
		buffer.put(message.getFrom());
		buffer.put(message.getTo());

		if (message instanceof ProtocolChat) {
			ProtocolChat protocolChat = (ProtocolChat) message;
			buffer.put(protocolChat.getData());
		} else if (message instanceof ProtocolFile) {
			ProtocolFile protocolFile = (ProtocolFile) message;
			buffer.putShort(protocolFile.getFileNameLength());
			buffer.put(protocolFile.getFileName());
			buffer.putLong(protocolFile.getFileSize());
			buffer.put(protocolFile.getFileMd5());
			buffer.putShort(protocolFile.getOffset());
			buffer.put(protocolFile.getData());
		}

		buffer.put(message.getCrc());
		buffer.put(Protocol.FINAL_TAG);

		buffer.flip();
		out.write(buffer);
	}

}
