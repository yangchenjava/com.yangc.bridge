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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yangc.bridge.bean.ResultBean;
import com.yangc.bridge.bean.TBridgeChat;
import com.yangc.bridge.bean.TBridgeCommon;
import com.yangc.bridge.bean.TBridgeFile;
import com.yangc.bridge.comm.cache.SessionCache;
import com.yangc.bridge.comm.handler.SendHandler;
import com.yangc.bridge.comm.protocol.ContentType;
import com.yangc.bridge.service.CommonService;
import com.yangc.utils.encryption.Md5Utils;

@Service
public class ChatAndFileProcessor {

	private static final Map<String, LinkedBlockingQueue<TBridgeCommon>> COMMON_QUEUE = new HashMap<String, LinkedBlockingQueue<TBridgeCommon>>();

	@Autowired
	private SessionCache sessionCache;
	@Autowired
	private CommonService commonService;

	private ExecutorService executorService;

	public ChatAndFileProcessor() {
		this.executorService = Executors.newCachedThreadPool();
	}

	public void process(IoSession session, TBridgeCommon common) throws InterruptedException {
		String toUsername = common.getTo();
		synchronized (COMMON_QUEUE) {
			if (COMMON_QUEUE.containsKey(toUsername)) {
				LinkedBlockingQueue<TBridgeCommon> queue = COMMON_QUEUE.get(toUsername);
				queue.put(common);
			} else {
				LinkedBlockingQueue<TBridgeCommon> queue = new LinkedBlockingQueue<TBridgeCommon>();
				queue.put(common);
				COMMON_QUEUE.put(toUsername, queue);
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

		private void sendResult(TBridgeCommon common) throws Exception {
			Long fromSessionId = sessionCache.getSessionId(common.getFrom());
			if (fromSessionId != null) {
				ResultBean result = new ResultBean();
				result.setUuid(common.getUuid());
				result.setSuccess(true);
				result.setData("success");
				SendHandler.sendResult(this.service.getManagedSessions().get(fromSessionId), result);
			}
		}

		private void saveChat(TBridgeCommon common) {
			TBridgeCommon c = new TBridgeCommon();
			BeanUtils.copyProperties(common, c);
			commonService.addCommon(c);
			common.setId(c.getId());
		}

		@Override
		public void run() {
			Long toSessionId = sessionCache.getSessionId(this.toUsername);
			LinkedBlockingQueue<TBridgeCommon> queue = COMMON_QUEUE.get(this.toUsername);
			while (true) {
				try {
					while (!queue.isEmpty()) {
						TBridgeCommon common = queue.poll();
						if (common instanceof TBridgeChat) {
							this.sendResult(common);
							this.saveChat(common);

							TBridgeChat chat = (TBridgeChat) common;
							commonService.addChat(chat);
							if (toSessionId != null) {
								SendHandler.sendChat(this.service.getManagedSessions().get(toSessionId), chat);
							}
						} else if (common instanceof TBridgeFile) {
							TBridgeFile file = (TBridgeFile) common;
							if (file.getContentType() == ContentType.TRANSMIT_FILE) {
								File dir = new File(FileUtils.getTempDirectory(), "com.yangc.bridge/" + this.toUsername);
								if (!dir.exists() || !dir.isDirectory()) {
									dir.delete();
									dir.mkdirs();
								}
								File targetFile = new File(dir, file.getUuid());
								if (!targetFile.exists() || !targetFile.isFile()) {
									targetFile.delete();
									targetFile.createNewFile();
								}
								RandomAccessFile raf = new RandomAccessFile(targetFile, "rw");
								raf.seek(raf.length());
								raf.write(file.getData(), 0, file.getOffset());
								raf.close();

								if (targetFile.length() == file.getFileSize() && Md5Utils.getMD5String(targetFile).equals(file.getFileMd5())) {
									this.sendResult(common);
									this.saveChat(common);
									commonService.addFile(file);
								}
							}

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
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				synchronized (COMMON_QUEUE) {
					if (queue.isEmpty()) {
						COMMON_QUEUE.remove(this.toUsername);
						break;
					}
				}
			}
		}
	}

}
