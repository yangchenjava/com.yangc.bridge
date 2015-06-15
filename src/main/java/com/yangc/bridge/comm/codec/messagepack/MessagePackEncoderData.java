package com.yangc.bridge.comm.codec.messagepack;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.msgpack.MessagePack;

import com.yangc.bridge.comm.protocol.ContentType;
import com.yangc.bridge.comm.protocol.Tag;
import com.yangc.bridge.comm.protocol.messagepack.MessagePackChat;
import com.yangc.bridge.comm.protocol.messagepack.MessagePackFile;
import com.yangc.bridge.comm.protocol.messagepack.MessagePackHeart;
import com.yangc.bridge.comm.protocol.messagepack.MessagePackLogin;
import com.yangc.bridge.comm.protocol.messagepack.MessagePackResult;

public class MessagePackEncoderData extends ProtocolEncoderAdapter {

	private static final int CAPACITY = 4096;

	@Override
	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
		IoBuffer buffer = IoBuffer.allocate(CAPACITY).setAutoExpand(true);

		if (message instanceof MessagePackResult) {
			this.encodeResult(buffer, (MessagePackResult) message);
		} else if (message instanceof MessagePackLogin) {
			this.encodeLogin(buffer, (MessagePackLogin) message);
		} else if (message instanceof MessagePackChat) {
			this.encodeChat(buffer, (MessagePackChat) message);
		} else if (message instanceof MessagePackFile) {
			this.encodeFile(buffer, (MessagePackFile) message);
		} else if (message instanceof MessagePackHeart) {
			this.encodeHeart(buffer, (MessagePackHeart) message);
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

	private void encodeResult(IoBuffer buffer, MessagePackResult message) throws IOException {
		buffer.put(Tag.START);
		buffer.put(ContentType.RESULT);

		byte[] bytes = new MessagePack().write(message);
		buffer.putInt(bytes.length);
		buffer.put(bytes);
	}

	private void encodeLogin(IoBuffer buffer, MessagePackLogin message) throws IOException {
		buffer.put(Tag.START);
		buffer.put(ContentType.LOGIN);

		byte[] bytes = new MessagePack().write(message);
		buffer.putInt(bytes.length);
		buffer.put(bytes);
	}

	private void encodeChat(IoBuffer buffer, MessagePackChat message) throws IOException {
		buffer.put(Tag.START);
		buffer.put(ContentType.CHAT);

		byte[] bytes = new MessagePack().write(message);
		buffer.putInt(bytes.length);
		buffer.put(bytes);
	}

	private void encodeFile(IoBuffer buffer, MessagePackFile message) throws IOException {
		buffer.put(Tag.START);

		// 准备发送文件
		if (ArrayUtils.isEmpty(message.getData())) buffer.put(ContentType.READY_FILE);
		// 传输文件
		else buffer.put(ContentType.TRANSMIT_FILE);

		byte[] bytes = new MessagePack().write(message);
		buffer.putInt(bytes.length);
		buffer.put(bytes);
	}

	private void encodeHeart(IoBuffer buffer, MessagePackHeart message) {
		buffer.put(Tag.START);
		buffer.put(ContentType.HEART);
		buffer.put(Tag.END);
	}

}
