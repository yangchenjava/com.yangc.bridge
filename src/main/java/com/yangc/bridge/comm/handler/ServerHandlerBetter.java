package com.yangc.bridge.comm.handler;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;

import com.yangc.bridge.comm.cache.SessionCache;

public class ServerHandlerBetter extends IoHandlerAdapter {

	private static final Logger logger = Logger.getLogger(ServerHandlerBetter.class);

	@Autowired
	private SessionCache sessionCache;

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
		this.sessionCache.removeSessionId(session.getId());
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
	}

}
