package com.yangc.bridge.comm.handler.processor;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.io.FileUtils;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yangc.bridge.bean.ResultBean;
import com.yangc.bridge.bean.TBridgeChat;
import com.yangc.bridge.bean.TBridgeFile;
import com.yangc.bridge.bean.TBridgeText;
import com.yangc.bridge.comm.cache.SessionCache;
import com.yangc.bridge.comm.handler.SendHandler;
import com.yangc.bridge.comm.protocol.ContentType;
import com.yangc.bridge.service.ChatService;
import com.yangc.utils.encryption.Md5Utils;

@Service
public class TextAndFileProcessor {

	private static final Map<String, LinkedBlockingQueue<TBridgeChat>> CHAT_QUEUE = new HashMap<String, LinkedBlockingQueue<TBridgeChat>>();

	@Autowired
	private SessionCache sessionCache;
	@Autowired
	private ChatService chatService;

	private ExecutorService executorService;

	public TextAndFileProcessor() {
		this.executorService = Executors.newCachedThreadPool();
	}

	public void process(IoSession session, TBridgeChat chat) throws InterruptedException {
		String toUsername = chat.getTo();
		synchronized (CHAT_QUEUE) {
			if (CHAT_QUEUE.containsKey(toUsername)) {
				LinkedBlockingQueue<TBridgeChat> queue = CHAT_QUEUE.get(toUsername);
				queue.put(chat);
			} else {
				LinkedBlockingQueue<TBridgeChat> queue = new LinkedBlockingQueue<TBridgeChat>();
				queue.put(chat);
				CHAT_QUEUE.put(toUsername, queue);
				this.executorService.execute(new Task(session.getService(), toUsername));
			}
		}
	}

	class Task implements Runnable {
		private IoService service;
		private String toUsername;

		public Task(IoService service, String toUsername) {
			this.service = service;
			this.toUsername = toUsername;
		}

		private void sendResult(TBridgeChat chat) throws Exception {
			Long fromSessionId = sessionCache.getSessionId(chat.getFrom());
			if (fromSessionId != null) {
				ResultBean result = new ResultBean();
				result.setUuid(chat.getUuid());
				result.setSuccess(true);
				result.setData("success");
				SendHandler.sendResult(this.service.getManagedSessions().get(fromSessionId), result);
			}
		}

		@Override
		public void run() {
			try {
				Long toSessionId = sessionCache.getSessionId(this.toUsername);
				LinkedBlockingQueue<TBridgeChat> queue = CHAT_QUEUE.get(this.toUsername);
				while (true) {
					while (!queue.isEmpty()) {
						TBridgeChat chat = queue.poll();
						if (chat instanceof TBridgeText) {
							TBridgeText text = (TBridgeText) chat;
							if (toSessionId != null) {
								SendHandler.sendChat(this.service.getManagedSessions().get(toSessionId), text);
							}

							this.sendResult(chat);
							chatService.addChat(chat);
							chatService.addText(text);
						} else {
							TBridgeFile file = (TBridgeFile) chat;
							if (toSessionId != null) {
								switch (file.getContentType()) {
								case ContentType.READY_FILE:
									SendHandler.sendReadyFile(this.service.getManagedSessions().get(toSessionId), file);
									break;
								case ContentType.TRANSMIT_FILE:
									SendHandler.sendTransmitFile(this.service.getManagedSessions().get(toSessionId), file);
									break;
								}
							}

							if (file.getContentType() == ContentType.TRANSMIT_FILE) {
								File dir = new File(FileUtils.getTempDirectory(), "com.yangc.bridge/" + this.toUsername);
								if (!dir.exists() || !dir.isDirectory()) {
									dir.delete();
									dir.mkdirs();
								}
								File targetFile = new File(dir, file.getUuid());
								if (!targetFile.exists()) {
									targetFile.createNewFile();
								}
								RandomAccessFile raf = new RandomAccessFile(targetFile, "rw");
								raf.seek(raf.length());
								raf.write(file.getData(), 0, file.getOffset());
								raf.close();

								if (targetFile.length() == file.getFileSize() && Md5Utils.getMD5String(targetFile).equals(file.getFileMd5())) {
									this.sendResult(chat);
									chatService.addChat(chat);
									chatService.addFile(file);
								}
							}
						}
					}
					synchronized (CHAT_QUEUE) {
						if (queue.isEmpty()) {
							CHAT_QUEUE.remove(this.toUsername);
							break;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
