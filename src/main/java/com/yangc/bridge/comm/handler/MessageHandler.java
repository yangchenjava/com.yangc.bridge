package com.yangc.bridge.comm.handler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;

import com.yangc.bridge.bean.ResultBean;
import com.yangc.bridge.bean.TBridgeChat;
import com.yangc.bridge.bean.TBridgeFile;
import com.yangc.bridge.comm.Server;
import com.yangc.bridge.comm.protocol.ContentType;
import com.yangc.bridge.comm.protocol.TransmitStatus;
import com.yangc.bridge.comm.protocol.prototype.ProtocolChat;
import com.yangc.bridge.comm.protocol.prototype.ProtocolFile;
import com.yangc.bridge.comm.protocol.prototype.ProtocolResult;

public class MessageHandler implements Runnable {

	private static final Logger logger = Logger.getLogger(MessageHandler.class);

	public static void sendResult(IoSession session, ResultBean result) throws Exception {
		byte[] from = result.getFrom().getBytes(Server.CHARSET_NAME);
		byte[] to = result.getTo().getBytes(Server.CHARSET_NAME);
		byte[] data = result.getData().getBytes(Server.CHARSET_NAME);

		ProtocolResult protocol = new ProtocolResult();
		protocol.setContentType(ContentType.RESULT);
		protocol.setUuid(result.getUuid().getBytes(Server.CHARSET_NAME));
		protocol.setFromLength((short) from.length);
		protocol.setToLength((short) to.length);
		protocol.setDataLength(1 + data.length);
		protocol.setFrom(from);
		protocol.setTo(to);
		protocol.setSuccess((byte) (result.isSuccess() ? 1 : 0));
		protocol.setData(data);

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

	public static void sendFile(IoSession session, TBridgeFile file, boolean success) {
		new Thread(new MessageHandler(session, file, success)).start();
	}

	private IoSession session;
	private TBridgeFile file;
	private boolean success;

	private MessageHandler(IoSession session, TBridgeFile file, boolean success) {
		this.session = session;
		this.file = file;
		this.success = success;
	}

	@Override
	public void run() {
		File offlineFile = new File(FileUtils.getTempDirectoryPath() + "/com.yangc.bridge/" + this.file.getTo() + "/" + this.file.getUuid());
		if (this.success) {
			BufferedInputStream bis = null;
			try {
				byte[] from = this.file.getFrom().getBytes(Server.CHARSET_NAME);
				byte[] to = this.file.getTo().getBytes(Server.CHARSET_NAME);
				byte[] fileName = this.file.getFileName().getBytes(Server.CHARSET_NAME);
				int offset = -1;
				byte[] data = new byte[1024 * 1024];

				ProtocolFile protocol = new ProtocolFile();
				protocol.setContentType(ContentType.TRANSMIT_FILE);
				protocol.setUuid(this.file.getUuid().getBytes(Server.CHARSET_NAME));
				protocol.setFromLength((short) from.length);
				protocol.setToLength((short) to.length);
				protocol.setDataLength(fileName.length + 47 + data.length);
				protocol.setFrom(from);
				protocol.setTo(to);
				protocol.setTransmitStatus(TransmitStatus.OFFLINE);
				protocol.setFileNameLength((short) fileName.length);
				protocol.setFileName(fileName);
				protocol.setFileSize(this.file.getFileSize());
				protocol.setFileMd5(this.file.getFileMd5().getBytes(Server.CHARSET_NAME));

				bis = new BufferedInputStream(new FileInputStream(offlineFile));
				while ((offset = bis.read(data)) != -1) {
					protocol.setOffset(offset);
					protocol.setData(data);
					if (!session.write(protocol).awaitUninterruptibly().isWritten()) {
						logger.info("sendFile - write file error");
						break;
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (bis != null) bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		offlineFile.delete();
	}

}
