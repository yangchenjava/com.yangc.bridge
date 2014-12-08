package com.yangc.bridge.comm.handler;

import java.net.InetAddress;
import java.net.InetSocketAddress;

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
import com.yangc.bridge.comm.handler.processor.ChatAndFileProcessor;
import com.yangc.bridge.comm.handler.processor.LoginProcessor;
import com.yangc.bridge.comm.handler.processor.ResultProcessor;

@Service
public class ServerHandler extends IoHandlerAdapter {

	private static final Logger logger = Logger.getLogger(ServerHandler.class);

	public static final String USER = "user";
	public static final String LOGIN_COUNT = "login_count";

	@Autowired
	private SessionCache sessionCache;
	@Autowired
	private ResultProcessor resultProcessor;
	@Autowired
	private LoginProcessor loginProcessor;
	@Autowired
	private ChatAndFileProcessor chatAndFileProcessor;

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		String remoteAddress = "";
		if (session.getRemoteAddress() != null) {
			InetAddress address = ((InetSocketAddress) session.getRemoteAddress()).getAddress();
			if (address != null) {
				remoteAddress = address.getHostAddress();
			}
		}
		logger.info("sessionCreated - " + remoteAddress);
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		String remoteAddress = "";
		if (session.getRemoteAddress() != null) {
			InetAddress address = ((InetSocketAddress) session.getRemoteAddress()).getAddress();
			if (address != null) {
				remoteAddress = address.getHostAddress();
			}
		}
		logger.info("sessionOpened - " + remoteAddress);
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		String remoteAddress = "";
		if (session.getRemoteAddress() != null) {
			InetAddress address = ((InetSocketAddress) session.getRemoteAddress()).getAddress();
			if (address != null) {
				remoteAddress = address.getHostAddress();
			}
		}
		logger.info("sessionClosed - " + remoteAddress);
		// 移除缓存
		UserBean user = (UserBean) session.getAttribute(USER);
		if (user != null) {
			this.sessionCache.removeSessionId(user.getUsername());
		}
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		String remoteAddress = "";
		if (session.getRemoteAddress() != null) {
			InetAddress address = ((InetSocketAddress) session.getRemoteAddress()).getAddress();
			if (address != null) {
				remoteAddress = address.getHostAddress();
			}
		}
		logger.info("sessionIdle - " + remoteAddress);
		if (status.equals(IdleStatus.BOTH_IDLE)) {
			session.close(true);
		}
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		String remoteAddress = "";
		if (session.getRemoteAddress() != null) {
			InetAddress address = ((InetSocketAddress) session.getRemoteAddress()).getAddress();
			if (address != null) {
				remoteAddress = address.getHostAddress();
			}
		}
		logger.error("exceptionCaught - " + remoteAddress + cause.getMessage(), cause);
		session.close(true);
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		logger.info("messageReceived");
		if (message instanceof Byte) {
			SendHandler.sendHeart(session);
		} else if (message instanceof ResultBean) {
			this.resultReceived(session, (ResultBean) message);
		} else if (message instanceof UserBean) {
			this.loginReceived(session, (UserBean) message);
		} else if (message instanceof TBridgeChat) {
			this.chatReceived(session, (TBridgeChat) message);
		} else if (message instanceof TBridgeFile) {
			this.fileReceived(session, (TBridgeFile) message);
		} else {
			session.close(true);
		}
	}

	private void resultReceived(IoSession session, ResultBean result) throws Exception {
		if (this.validateAuthentication(session)) {
			this.resultProcessor.process(session, result);
		} else {
			session.close(true);
		}
	}

	private void loginReceived(IoSession session, UserBean user) throws Exception {
		this.loginProcessor.process(session, user);
	}

	private void chatReceived(IoSession session, TBridgeChat chat) throws Exception {
		if (this.validateAuthentication(session)) {
			this.chatAndFileProcessor.process(session, chat);
		} else {
			session.close(true);
		}
	}

	private void fileReceived(IoSession session, TBridgeFile file) throws Exception {
		if (this.validateAuthentication(session)) {
			this.chatAndFileProcessor.process(session, file);
		} else {
			session.close(true);
		}
	}

	private boolean validateAuthentication(IoSession session) {
		UserBean user = (UserBean) session.getAttribute(USER);
		if (user != null && this.sessionCache.contains(user.getUsername())) {
			return true;
		}
		return false;
	}

}
