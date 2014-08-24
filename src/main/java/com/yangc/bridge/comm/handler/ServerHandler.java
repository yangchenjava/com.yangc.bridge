package com.yangc.bridge.comm.handler;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yangc.bridge.bean.ResultBean;
import com.yangc.bridge.bean.TBridgeChat;
import com.yangc.bridge.bean.TBridgeFile;
import com.yangc.bridge.bean.UserBean;
import com.yangc.bridge.comm.cache.SessionCache;
import com.yangc.bridge.comm.protocol.ProtocolResult;
import com.yangc.bridge.service.ChatService;
import com.yangc.system.bean.TSysUser;
import com.yangc.system.service.UserService;
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
		SessionCache.removeSession(session.getRemoteAddress().toString());
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
		logger.info("exceptionCaught");
		session.close(true);
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		logger.info("messageReceived");
		if (message instanceof ResultBean) {
			ResultBean result = (ResultBean) message;

			ProtocolResult protocol = new ProtocolResult();
			protocol.setContentType((byte) 0);
			protocol.setUuid(result.getUuid().getBytes(CHARSET_NAME));
			protocol.setToLength((short) result.getTo().getBytes(CHARSET_NAME).length);
			protocol.setDataLength((short) (1 + result.getMessage().getBytes(CHARSET_NAME).length));
			protocol.setTo(result.getTo().getBytes(CHARSET_NAME));
			protocol.setSuccess((byte) (result.isSuccess() ? 1 : 0));
			protocol.setMessage(result.getMessage().getBytes(CHARSET_NAME));

			session.write(protocol);
		} else if (message instanceof UserBean) {
			UserBean user = (UserBean) message;
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
				SessionCache.putSession(user.getUsername() + "@" + session.getRemoteAddress(), session);

				protocol.setSuccess((byte) 1);
				protocol.setMessage("登录成功".getBytes(CHARSET_NAME));
			}
			protocol.setDataLength((short) (1 + protocol.getMessage().length));

			session.write(protocol);
		} else if (message instanceof TBridgeChat) {
			TBridgeChat chatBean = (TBridgeChat) message;
			System.out.println(chatBean.getUuid());
			System.out.println(chatBean.getFrom());
			System.out.println(chatBean.getTo());
			System.out.println(chatBean.getData());
		} else if (message instanceof TBridgeFile) {

		}
	}

}
