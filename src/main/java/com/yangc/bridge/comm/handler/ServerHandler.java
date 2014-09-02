package com.yangc.bridge.comm.handler;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yangc.bridge.bean.FileBean;
import com.yangc.bridge.bean.ResultBean;
import com.yangc.bridge.bean.TBridgeChat;
import com.yangc.bridge.bean.UserBean;
import com.yangc.bridge.comm.Server;
import com.yangc.bridge.comm.cache.SessionCache;
import com.yangc.bridge.comm.protocol.ContentType;
import com.yangc.bridge.comm.protocol.ProtocolChat;
import com.yangc.bridge.comm.protocol.ProtocolFile;
import com.yangc.bridge.comm.protocol.ProtocolHeart;
import com.yangc.bridge.comm.protocol.ProtocolResult;
import com.yangc.bridge.service.ChatService;
import com.yangc.system.bean.TSysUser;
import com.yangc.system.service.UserService;
import com.yangc.utils.Message;
import com.yangc.utils.encryption.Md5Utils;

@Service
public class ServerHandler extends IoHandlerAdapter {

	private static final Logger logger = Logger.getLogger(ServerHandler.class);

	private static final ConcurrentMap<String, TBridgeChat> CHAT_CACHE = new ConcurrentHashMap<String, TBridgeChat>();

	@Autowired
	private UserService userService;
	@Autowired
	private ChatService chatService;

	public ServerHandler() {
		new Thread(new ChatCheck()).start();
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		logger.info("sessionClosed - " + ((InetSocketAddress) session.getRemoteAddress()).getAddress().getHostAddress());
		// 移除缓存
		SessionCache.removeSessionId(session.getId());
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		logger.info("sessionIdle");
		if (status.equals(IdleStatus.BOTH_IDLE)) {
			session.close(true);
		}
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		logger.error("exceptionCaught - " + cause.getMessage(), cause);
		session.close(true);
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		logger.info("messageReceived");
		if (message instanceof ProtocolHeart) {
			session.write(message);
		} else if (message instanceof ResultBean) {
			this.resultReceived(session, (ResultBean) message);
		} else if (message instanceof UserBean) {
			this.loginReceived(session, (UserBean) message);
		} else if (message instanceof TBridgeChat) {
			this.chatReceived(session, (TBridgeChat) message);
		} else if (message instanceof FileBean) {
			this.fileReceived(session, (FileBean) message);
		} else {
			session.close(true);
		}
	}

	/**
	 * @功能: 接收结果并转发
	 * @作者: yangc
	 * @创建日期: 2014年8月26日 上午10:39:03
	 * @param session
	 * @param result
	 * @throws Exception
	 */
	private void resultReceived(IoSession session, ResultBean result) throws Exception {
		TBridgeChat chat = CHAT_CACHE.remove(result.getUuid());
		if (chat != null && chat.getId() == null) this.chatService.addOrUpdateChat(null, chat.getUuid(), chat.getFrom(), chat.getTo(), chat.getData(), 1L);

		Long sessionId = SessionCache.getSessionId(result.getTo());
		if (sessionId != null) {
			byte[] to = result.getTo().getBytes(Server.CHARSET_NAME);
			byte[] message = result.getMessage().getBytes(Server.CHARSET_NAME);

			ProtocolResult protocol = new ProtocolResult();
			protocol.setContentType(ContentType.RESULT);
			protocol.setUuid(result.getUuid().getBytes(Server.CHARSET_NAME));
			protocol.setToLength((short) to.length);
			protocol.setDataLength(1 + message.length);
			protocol.setTo(to);
			protocol.setSuccess((byte) (result.isSuccess() ? 1 : 0));
			protocol.setMessage(message);

			session.getService().getManagedSessions().get(sessionId).write(protocol);
		}
	}

	/**
	 * @功能: 验证登录, 并发送验证结果, 如果登录成功, 并且支持离线消息, 转发存在的离线消息
	 * @作者: yangc
	 * @创建日期: 2014年8月26日 上午10:39:37
	 * @param session
	 * @param user
	 * @throws Exception
	 */
	private void loginReceived(IoSession session, UserBean user) throws Exception {
		List<TSysUser> users = this.userService.getUserListByUsernameAndPassword(user.getUsername(), Md5Utils.getMD5(user.getPassword()));

		ProtocolResult protocol = new ProtocolResult();
		protocol.setContentType(ContentType.RESULT);
		protocol.setUuid(user.getUuid().getBytes(Server.CHARSET_NAME));
		protocol.setToLength((short) user.getUsername().getBytes(Server.CHARSET_NAME).length);
		protocol.setTo(user.getUsername().getBytes(Server.CHARSET_NAME));
		if (users == null || users.isEmpty()) {
			protocol.setSuccess((byte) 0);
			protocol.setMessage("用户名或密码错误".getBytes(Server.CHARSET_NAME));
		} else if (users.size() > 1) {
			protocol.setSuccess((byte) 0);
			protocol.setMessage("用户重复".getBytes(Server.CHARSET_NAME));
		} else {
			// 添加缓存
			SessionCache.putSessionId(user.getUsername(), session.getId());

			protocol.setSuccess((byte) 1);
			protocol.setMessage("登录成功".getBytes(Server.CHARSET_NAME));
		}
		protocol.setDataLength(1 + protocol.getMessage().length);

		session.write(protocol);

		// 登录失败, 标记登录次数, 超过登录阀值就踢出
		if (protocol.getSuccess() == 0) {
			Integer loginCount = (Integer) session.getAttribute("loginCount", 1);
			if (loginCount > 2) {
				session.close(false);
			} else {
				session.setAttribute("loginCount", ++loginCount);
			}
		}
		// 登录成功, 如果存在离线消息, 则发送
		else if (StringUtils.equals(Message.getMessage("bridge.offline_data"), "1")) {
			List<TBridgeChat> chatList = this.chatService.getUnreadChatListByTo(user.getUsername());
			if (chatList != null && !chatList.isEmpty()) {
				List<Long> ids = new ArrayList<Long>(chatList.size());
				for (TBridgeChat chat : chatList) {
					byte[] from = chat.getFrom().getBytes(Server.CHARSET_NAME);
					byte[] to = chat.getTo().getBytes(Server.CHARSET_NAME);
					byte[] data = chat.getData().getBytes(Server.CHARSET_NAME);

					ProtocolChat protocolChat = new ProtocolChat();
					protocolChat.setContentType(ContentType.CHAT);
					protocolChat.setUuid(chat.getUuid().getBytes(Server.CHARSET_NAME));
					protocolChat.setFromLength((short) from.length);
					protocolChat.setToLength((short) to.length);
					protocolChat.setDataLength(data.length);
					protocolChat.setFrom(from);
					protocolChat.setTo(to);
					protocolChat.setData(data);

					session.write(protocolChat);
					chat.setMillisecond(System.currentTimeMillis());
					CHAT_CACHE.put(chat.getUuid(), chat);
					ids.add(chat.getId());
				}
				this.chatService.updateChatStatus(ids);
			}
		}
	}

	/**
	 * @功能: 接收消息并转发
	 * @作者: yangc
	 * @创建日期: 2014年9月2日 下午6:32:45
	 * @param session
	 * @param chat
	 * @throws Exception
	 */
	private void chatReceived(IoSession session, TBridgeChat chat) throws Exception {
		Long sessionId = SessionCache.getSessionId(chat.getTo());
		if (sessionId != null) {
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

			session.getService().getManagedSessions().get(sessionId).write(protocol);
			chat.setMillisecond(System.currentTimeMillis());
			CHAT_CACHE.put(chat.getUuid(), chat);
		} else if (StringUtils.equals(Message.getMessage("bridge.offline_data"), "1")) {
			this.chatService.addOrUpdateChat(null, chat.getUuid(), chat.getFrom(), chat.getTo(), chat.getData(), 0L);
		}
	}

	/**
	 * @功能: 文件确认, 文件传输转发
	 * @作者: yangc
	 * @创建日期: 2014年9月2日 下午6:33:23
	 * @param session
	 * @param file
	 * @throws Exception
	 */
	private void fileReceived(IoSession session, FileBean file) throws Exception {
		Long sessionId = SessionCache.getSessionId(file.getTo());
		if (sessionId != null) {
			byte[] from = file.getFrom().getBytes(Server.CHARSET_NAME);
			byte[] to = file.getTo().getBytes(Server.CHARSET_NAME);
			byte[] fileName = file.getFileName().getBytes(Server.CHARSET_NAME);

			ProtocolFile protocol = new ProtocolFile();
			byte contentType = file.getContentType();
			if (contentType == ContentType.READY_FILE) {
				protocol.setContentType(contentType);
				protocol.setUuid(file.getUuid().getBytes(Server.CHARSET_NAME));
				protocol.setFromLength((short) from.length);
				protocol.setToLength((short) to.length);
				protocol.setFrom(from);
				protocol.setTo(to);
				protocol.setFileNameLength((short) fileName.length);
				protocol.setFileName(fileName);
				protocol.setFileSize(file.getFileSize());
			} else if (contentType == ContentType.TRANSMIT_FILE) {
				protocol.setContentType(contentType);
				protocol.setUuid(file.getUuid().getBytes(Server.CHARSET_NAME));
				protocol.setFromLength((short) from.length);
				protocol.setToLength((short) to.length);
				protocol.setDataLength(fileName.length + 46 + file.getData().length);
				protocol.setFrom(from);
				protocol.setTo(to);
				protocol.setFileNameLength((short) fileName.length);
				protocol.setFileName(fileName);
				protocol.setFileSize(file.getFileSize());
				protocol.setFileMd5(file.getFileMd5().getBytes(Server.CHARSET_NAME));
				protocol.setOffset(file.getOffset());
				protocol.setData(file.getData());
			}
			session.getService().getManagedSessions().get(sessionId).write(protocol);
		}
	}

	/**
	 * @功能: 超时无响应, 标记为未读
	 * @作者: yangc
	 * @创建日期: 2014年9月2日 下午6:33:23
	 */
	class ChatCheck implements Runnable {
		@Override
		public void run() {
			while (true) {
				try {
					long currentMillisecond = System.currentTimeMillis();
					for (Entry<String, TBridgeChat> entry : CHAT_CACHE.entrySet()) {
						TBridgeChat chat = entry.getValue();
						if (currentMillisecond - chat.getMillisecond() > 8000) {
							logger.info("unread - uuid=" + chat.getUuid() + ", from=" + chat.getFrom() + ", to=" + chat.getTo());
							ServerHandler.this.chatService.addOrUpdateChat(chat.getId(), chat.getUuid(), chat.getFrom(), chat.getTo(), chat.getData(), 0L);
							CHAT_CACHE.remove(entry.getKey());
						}
					}
					Thread.sleep(5000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}
