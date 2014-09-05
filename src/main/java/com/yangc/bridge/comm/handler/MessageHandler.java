package com.yangc.bridge.comm.handler;

import org.apache.mina.core.session.IoSession;

import com.yangc.bridge.bean.ResultBean;
import com.yangc.bridge.bean.TBridgeChat;
import com.yangc.bridge.bean.TBridgeFile;
import com.yangc.bridge.comm.Server;
import com.yangc.bridge.comm.protocol.ContentType;
import com.yangc.bridge.comm.protocol.ProtocolChat;
import com.yangc.bridge.comm.protocol.ProtocolFile;
import com.yangc.bridge.comm.protocol.ProtocolResult;

public class MessageHandler {

	public static void sendResult(IoSession session, ResultBean result) throws Exception {
		byte[] from = result.getFrom().getBytes(Server.CHARSET_NAME);
		byte[] to = result.getTo().getBytes(Server.CHARSET_NAME);
		byte[] message = result.getMessage().getBytes(Server.CHARSET_NAME);

		ProtocolResult protocol = new ProtocolResult();
		protocol.setContentType(ContentType.RESULT);
		protocol.setUuid(result.getUuid().getBytes(Server.CHARSET_NAME));
		protocol.setFromLength((short) from.length);
		protocol.setToLength((short) to.length);
		protocol.setDataLength(1 + message.length);
		protocol.setFrom(from);
		protocol.setTo(to);
		protocol.setSuccess((byte) (result.isSuccess() ? 1 : 0));
		protocol.setMessage(message);

		session.write(protocol);
	}

	public static void sendChat(IoSession session, TBridgeChat chat) throws Exception {
		byte[] from = chat.getFrom().getBytes(Server.CHARSET_NAME);
		byte[] to = chat.getTo().getBytes(Server.CHARSET_NAME);
		byte[] data = chat.getData().getBytes(Server.CHARSET_NAME);

		ProtocolChat protocol = new ProtocolChat();
		protocol.setContentType(ContentType.CHAT);
		protocol.setUuid(chat.getUuid().getBytes(Server.CHARSET_NAME));
		protocol.setFromLength((short) from.length);
		protocol.setToLength((short) to.length);
		protocol.setDataLength(data.length);
		protocol.setFrom(from);
		protocol.setTo(to);
		protocol.setData(data);

		session.write(protocol);
	}

	public static void sendReadyFile(IoSession session, TBridgeFile file) throws Exception {
		byte[] from = file.getFrom().getBytes(Server.CHARSET_NAME);
		byte[] to = file.getTo().getBytes(Server.CHARSET_NAME);
		byte[] fileName = file.getFileName().getBytes(Server.CHARSET_NAME);

		ProtocolFile protocol = new ProtocolFile();
		protocol.setContentType(ContentType.READY_FILE);
		protocol.setUuid(file.getUuid().getBytes(Server.CHARSET_NAME));
		protocol.setFromLength((short) from.length);
		protocol.setToLength((short) to.length);
		protocol.setFrom(from);
		protocol.setTo(to);
		protocol.setFileNameLength((short) fileName.length);
		protocol.setFileName(fileName);
		protocol.setFileSize(file.getFileSize());

		session.write(protocol);
	}

	public static void sendTransmitFile(IoSession session, TBridgeFile file) throws Exception {
		byte[] from = file.getFrom().getBytes(Server.CHARSET_NAME);
		byte[] to = file.getTo().getBytes(Server.CHARSET_NAME);
		byte[] fileName = file.getFileName().getBytes(Server.CHARSET_NAME);

		ProtocolFile protocol = new ProtocolFile();
		protocol.setContentType(ContentType.TRANSMIT_FILE);
		protocol.setUuid(file.getUuid().getBytes(Server.CHARSET_NAME));
		protocol.setFromLength((short) from.length);
		protocol.setToLength((short) to.length);
		protocol.setDataLength(fileName.length + 47 + file.getData().length);
		protocol.setFrom(from);
		protocol.setTo(to);
		protocol.setTransmitStatus(file.getTransmitStatus());
		protocol.setFileNameLength((short) fileName.length);
		protocol.setFileName(fileName);
		protocol.setFileSize(file.getFileSize());
		protocol.setFileMd5(file.getFileMd5().getBytes(Server.CHARSET_NAME));
		protocol.setOffset(file.getOffset());
		protocol.setData(file.getData());

		session.write(protocol);
	}

}
