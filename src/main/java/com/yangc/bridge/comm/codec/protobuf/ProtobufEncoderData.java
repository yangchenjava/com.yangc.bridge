package com.yangc.bridge.comm.codec.protobuf;

import java.util.Arrays;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.yangc.bridge.comm.protocol.ContentType;
import com.yangc.bridge.comm.protocol.Tag;
import com.yangc.bridge.comm.protocol.protobuf.ProtobufMessage;

public class ProtobufEncoderData extends ProtocolEncoderAdapter {

	private static final int CAPACITY = 4096;

	@Override
	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
		IoBuffer buffer = IoBuffer.allocate(CAPACITY).setAutoExpand(true);

		if (message instanceof ProtobufMessage.Result) {
			this.encodeResult(buffer, (ProtobufMessage.Result) message);
		} else if (message instanceof ProtobufMessage.Login) {
			this.encodeLogin(buffer, (ProtobufMessage.Login) message);
		} else if (message instanceof ProtobufMessage.Chat) {
			this.encodeChat(buffer, (ProtobufMessage.Chat) message);
		} else if (message instanceof ProtobufMessage.File) {
			this.encodeFile(buffer, (ProtobufMessage.File) message);
		} else if (message instanceof ProtobufMessage.Heart) {
			this.encodeHeart(buffer, (ProtobufMessage.Heart) message);
		}

		byte crc = 0;
		byte[] b = Arrays.copyOfRange(buffer.array(), 0, buffer.position());
		for (int i = 0; i < b.length; i++) {
			crc += b[i];
		}
		buffer.put(crc);
		buffer.put(Tag.FINAL);

		buffer.flip();
		out.write(buffer);
	}

	private void encodeResult(IoBuffer buffer, ProtobufMessage.Result message) {
		buffer.put(Tag.START);
		buffer.put(ContentType.RESULT);
		buffer.putInt(message.getSerializedSize());
		buffer.put(message.toByteArray());
	}

	private void encodeLogin(IoBuffer buffer, ProtobufMessage.Login message) {
		buffer.put(Tag.START);
		buffer.put(ContentType.LOGIN);
		buffer.putInt(message.getSerializedSize());
		buffer.put(message.toByteArray());
	}

	private void encodeChat(IoBuffer buffer, ProtobufMessage.Chat message) {
		buffer.put(Tag.START);
		buffer.put(ContentType.CHAT);
		buffer.putInt(message.getSerializedSize());
		buffer.put(message.toByteArray());
	}

	private void encodeFile(IoBuffer buffer, ProtobufMessage.File message) {
		buffer.put(Tag.START);

		// 准备发送文件
		if (!message.hasData()) buffer.put(ContentType.READY_FILE);
		// 传输文件
		else buffer.put(ContentType.TRANSMIT_FILE);

		buffer.putInt(message.getSerializedSize());
		buffer.put(message.toByteArray());
	}

	private void encodeHeart(IoBuffer buffer, ProtobufMessage.Heart message) {
		buffer.put(Tag.START);
		buffer.put(ContentType.HEART);
		buffer.put(Tag.END);
	}

}
