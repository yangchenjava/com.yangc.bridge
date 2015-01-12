package com.yangc.bridge.comm.handler.processor;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.session.IoSession;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import com.yangc.bridge.bean.ResultBean;
import com.yangc.bridge.bean.TBridgeChat;
import com.yangc.bridge.bean.TBridgeCommon;
import com.yangc.bridge.bean.TBridgeFile;
import com.yangc.bridge.bean.UserBean;
import com.yangc.bridge.comm.Server;
import com.yangc.bridge.comm.cache.SessionCache;
import com.yangc.bridge.comm.handler.SendHandler;
import com.yangc.bridge.comm.handler.ServerHandler;
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
	@Autowired
	private JmsTemplate jmsTemplate;

	private ExecutorService executorService;

	public ChatAndFileProcessor() {
		this.executorService = Executors.newCachedThreadPool();
	}

	/**
	 * @功能: 处理消息转发逻辑
	 * @作者: yangc
	 * @创建日期: 2015年1月7日 下午5:31:33
	 * @param session
	 * @param common
	 * @throws InterruptedException
	 */
	public void process(IoSession session, TBridgeCommon common) throws InterruptedException {
		// 异步处理消息,每个用户有自己的消息队列,保证了同一用户的消息是有序的
		String toUsername = common.getTo();
		if (StringUtils.isNotBlank(toUsername)) {
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
	}

	private class Task implements Runnable {
		private IoService service;
		private String toUsername;

		private Task(IoService service, String toUsername) {
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

		private void saveCommon(TBridgeCommon common) {
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
							this.saveCommon(common);

							final TBridgeChat chat = (TBridgeChat) common;
							commonService.addChat(chat);
							if (toSessionId != null) {
								IoSession session = this.service.getManagedSessions().get(toSessionId);
								if (session != null && StringUtils.equals(((UserBean) session.getAttribute(ServerHandler.USER)).getUsername(), this.toUsername)) {
									SendHandler.sendChat(session, chat);
								} else {
									jmsTemplate.send(new MessageCreator() {
										@Override
										public Message createMessage(Session session) throws JMSException {
											ObjectMessage message = session.createObjectMessage();
											message.setStringProperty("IP", Server.IP);
											message.setObject(chat);
											return message;
										}
									});
								}
							}
						} else if (common instanceof TBridgeFile) {
							final TBridgeFile file = (TBridgeFile) common;
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
								RandomAccessFile raf = null;
								try {
									raf = new RandomAccessFile(targetFile, "rw");
									raf.seek(raf.length());
									raf.write(file.getData(), 0, file.getOffset());
								} catch (IOException e) {
									e.printStackTrace();
								} finally {
									try {
										if (raf != null) raf.close();
									} catch (IOException e) {
										e.printStackTrace();
									}
								}

								if (targetFile.length() == file.getFileSize() && Md5Utils.getMD5String(targetFile).equals(file.getFileMd5())) {
									this.sendResult(common);
									this.saveCommon(common);
									commonService.addFile(file);
								}
							}

							if (toSessionId != null) {
								IoSession session = this.service.getManagedSessions().get(toSessionId);
								if (session != null && StringUtils.equals(((UserBean) session.getAttribute(ServerHandler.USER)).getUsername(), this.toUsername)) {
									switch (file.getContentType()) {
									case ContentType.READY_FILE:
										SendHandler.sendReadyFile(session, file);
										break;
									case ContentType.TRANSMIT_FILE:
										SendHandler.sendTransmitFile(session, file);
										break;
									}
								} else {
									jmsTemplate.send(new MessageCreator() {
										@Override
										public Message createMessage(Session session) throws JMSException {
											ObjectMessage message = session.createObjectMessage();
											message.setStringProperty("IP", Server.IP);
											message.setObject(file);
											return message;
										}
									});
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
