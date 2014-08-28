package com.yangc.bridge.comm.handler;

import java.util.ArrayList;
import java.util.List;

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
import com.yangc.bridge.comm.cache.SessionCache;
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

	private static final String CHARSET_NAME = "UTF-8";

	@Autowired
	private UserService userService;
	@Autowired
	private ChatService chatService;

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		logger.info("sessionClosed");
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
		byte[] to = result.getTo().getBytes(CHARSET_NAME);
		byte[] message = result.getMessage().getBytes(CHARSET_NAME);

		ProtocolResult protocol = new ProtocolResult();
		protocol.setContentType((byte) 0);
		protocol.setUuid(result.getUuid().getBytes(CHARSET_NAME));
		protocol.setToLength((short) to.length);
		protocol.setDataLength(1 + message.length);
		protocol.setTo(to);
		protocol.setSuccess((byte) (result.isSuccess() ? 1 : 0));
		protocol.setMessage(message);

		Long sessionId = SessionCache.getSessionId(result.getTo());
		if (sessionId != null) {
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
		protocol.setContentType((byte) 0);
		protocol.setUuid(user.getUuid().getBytes(CHARSET_NAME));
		protocol.setToLength((short) user.getUsername().getBytes(CHARSET_NAME).length);
		protocol.setTo(user.getUsername().getBytes(CHARSET_NAME));
		if (users == null || users.isEmpty()) {
			protocol.setSuccess((byte) 0);
			protocol.setMessage("用户名或密码错误".getBytes(CHARSET_NAME));
		} else if (users.size() > 1) {
			protocol.setSuccess((byte) 0);
			protocol.setMessage("用户重复".getBytes(CHARSET_NAME));
		} else {
			// 添加缓存
			SessionCache.putSessionId(user.getUsername(), session.getId());

			protocol.setSuccess((byte) 1);
			protocol.setMessage("登录成功".getBytes(CHARSET_NAME));
		}
		protocol.setDataLength(1 + protocol.getMessage().length);

		session.write(protocol);

		if (protocol.getSuccess() == 1 && StringUtils.equals(Message.getMessage("bridge.offline_data"), "1")) {
			List<TBridgeChat> chatList = this.chatService.getUnreadChatListByTo(user.getUsername());
			if (chatList != null && !chatList.isEmpty()) {
				List<Long> ids = new ArrayList<Long>(chatList.size());
				for (TBridgeChat chat : chatList) {
					byte[] from = chat.getFrom().getBytes(CHARSET_NAME);
					byte[] to = chat.getTo().getBytes(CHARSET_NAME);
					byte[] data = chat.getData().getBytes(CHARSET_NAME);

					ProtocolChat protocolChat = new ProtocolChat();
					protocolChat.setContentType((byte) 2);
					protocolChat.setUuid(chat.getUuid().getBytes(CHARSET_NAME));
					protocolChat.setFromLength((short) from.length);
					protocolChat.setToLength((short) to.length);
					protocolChat.setDataLength(data.length);
					protocolChat.setFrom(from);
					protocolChat.setTo(to);
					protocolChat.setData(data);

					session.write(protocolChat);
					ids.add(chat.getId());
				}
				this.chatService.updateChatStatus(ids);
			}
		}
	}

	private void chatReceived(IoSession session, TBridgeChat chat) throws Exception {
		Long sessionId = SessionCache.getSessionId(chat.getTo()), status = 0L;
		if (sessionId != null) {
			byte[] from = chat.getFrom().getBytes(CHARSET_NAME);
			byte[] to = chat.getTo().getBytes(CHARSET_NAME);
			byte[] data = chat.getData().getBytes(CHARSET_NAME);

			ProtocolChat protocol = new ProtocolChat();
			protocol.setContentType((byte) 2);
			protocol.setUuid(chat.getUuid().getBytes(CHARSET_NAME));
			protocol.setFromLength((short) from.length);
			protocol.setToLength((short) to.length);
			protocol.setDataLength(data.length);
			protocol.setFrom(from);
			protocol.setTo(to);
			protocol.setData(data);

			session.getService().getManagedSessions().get(sessionId).write(protocol);
			status = 1L;
		}

		if (sessionId == null && StringUtils.equals(Message.getMessage("bridge.offline_data"), "0")) {
			return;
		}
		this.chatService.addOrUpdateChat(null, chat.getUuid(), chat.getFrom(), chat.getTo(), chat.getData(), status);
	}

	private void fileReceived(IoSession session, FileBean file) throws Exception {
		Long sessionId = SessionCache.getSessionId(file.getTo());
		if (sessionId != null) {
			byte[] from = file.getFrom().getBytes(CHARSET_NAME);
			byte[] to = file.getTo().getBytes(CHARSET_NAME);
			byte[] fileName = file.getFileName().getBytes(CHARSET_NAME);

			ProtocolFile protocol = new ProtocolFile();
			byte contentType = file.getContentType();
			if (contentType == 3) {
				protocol.setContentType(contentType);
				protocol.setUuid(file.getUuid().getBytes(CHARSET_NAME));
				protocol.setFromLength((short) from.length);
				protocol.setToLength((short) to.length);
				protocol.setFrom(from);
				protocol.setTo(to);
				protocol.setFileNameLength((short) fileName.length);
				protocol.setFileName(fileName);
				protocol.setFileSize(file.getFileSize());
			} else if (contentType == 4) {
				protocol.setContentType(contentType);
				protocol.setUuid(file.getUuid().getBytes(CHARSET_NAME));
				protocol.setFromLength((short) from.length);
				protocol.setToLength((short) to.length);
				protocol.setDataLength(fileName.length + 46 + file.getData().length);
				protocol.setFrom(from);
				protocol.setTo(to);
				protocol.setFileNameLength((short) fileName.length);
				protocol.setFileName(fileName);
				protocol.setFileSize(file.getFileSize());
				protocol.setFileMd5(file.getFileMd5().getBytes(CHARSET_NAME));
				protocol.setOffset(file.getOffset());
				protocol.setData(file.getData());
			}
			session.getService().getManagedSessions().get(sessionId).write(protocol);
		}
	}

}
