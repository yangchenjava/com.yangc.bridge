package com.yangc.bridge.comm.codec;

import java.util.Arrays;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.MessageEncoder;

import com.yangc.bridge.comm.protocol.Protocol;
import com.yangc.bridge.comm.protocol.ProtocolChat;
import com.yangc.bridge.comm.protocol.ProtocolFile;

public class DataEncoder implements MessageEncoder<Protocol> {

	private static final int CAPACITY = 4096;

	@Override
	public void encode(IoSession session, Protocol message, ProtocolEncoderOutput out) throws Exception {
		IoBuffer buffer = IoBuffer.allocate(CAPACITY).setAutoExpand(true);

		buffer.put(Protocol.START_TAG);
		buffer.put(message.getContentType());
		buffer.put(message.getUuid());
		buffer.putShort(message.getFromLength());
		buffer.putShort(message.getToLength());
		buffer.putShort(message.getDataLength());
		buffer.put(message.getFrom());
		buffer.put(message.getTo());
		buffer.put(Protocol.END_TAG);

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

}
