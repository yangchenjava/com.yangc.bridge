package com.yangc.bridge.comm.handler;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import com.yangc.bridge.bean.TBridgeChat;
import com.yangc.bridge.bean.TBridgeFile;

public class ServerHandler implements IoHandler {

	private static final Logger logger = Logger.getLogger(ServerHandler.class);

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		logger.info("sessionCreated");
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		logger.info("sessionOpened");
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		logger.info("sessionClosed");
		// 移除缓存
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
		if (message instanceof TBridgeChat) {
			TBridgeChat chatBean = (TBridgeChat) message;
			System.out.println(chatBean.getUuid());
			System.out.println(chatBean.getFrom());
			System.out.println(chatBean.getTo());
			System.out.println(chatBean.getData());
		} else if (message instanceof TBridgeFile) {

		}
	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		logger.info("messageSent");
	}

}
