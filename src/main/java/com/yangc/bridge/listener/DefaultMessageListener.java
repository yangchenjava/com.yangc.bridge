package com.yangc.bridge.listener;

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;

import com.yangc.bridge.bean.TBridgeChat;
import com.yangc.bridge.bean.TBridgeFile;
import com.yangc.bridge.bean.UserBean;
import com.yangc.bridge.comm.Server;
import com.yangc.bridge.comm.cache.SessionCache;
import com.yangc.bridge.comm.handler.SendHandler;
import com.yangc.bridge.comm.handler.ServerHandler;
import com.yangc.bridge.comm.protocol.ContentType;

public class DefaultMessageListener implements MessageListener {

	private static final Logger logger = Logger.getLogger(DefaultMessageListener.class);

	@Autowired
	private Server server;
	@Autowired
	private SessionCache sessionCache;

	@Override
	public void onMessage(Message message) {
		if (message != null) {
			try {
				if (message instanceof TextMessage) {
					logger.info("MessageListener - Text=" + ((TextMessage) message).getText());
				} else if (message instanceof ObjectMessage) {
					ObjectMessage msg = (ObjectMessage) message;
					if (!StringUtils.equals(msg.getStringProperty("IP"), Server.IP)) {
						Serializable obj = msg.getObject();
						if (obj instanceof UserBean) {
							UserBean user = (UserBean) obj;
							IoSession session = this.server.getManagedSessions().get(user.getSessionId());
							if (session != null && StringUtils.equals(((UserBean) session.getAttribute(ServerHandler.USER)).getUsername(), user.getUsername())) {
								session.close(true);
							}
						} else if (obj instanceof TBridgeChat) {
							TBridgeChat chat = (TBridgeChat) obj;
							Long sessionId = this.sessionCache.getSessionId(chat.getTo());
							if (sessionId != null) {
								IoSession session = this.server.getManagedSessions().get(sessionId);
								if (session != null && StringUtils.equals(((UserBean) session.getAttribute(ServerHandler.USER)).getUsername(), chat.getTo())) {
									SendHandler.sendChat(session, chat);
								}
							}
						} else if (obj instanceof TBridgeFile) {
							TBridgeFile file = (TBridgeFile) obj;
							Long sessionId = this.sessionCache.getSessionId(file.getTo());
							if (sessionId != null) {
								IoSession session = this.server.getManagedSessions().get(sessionId);
								if (session != null && StringUtils.equals(((UserBean) session.getAttribute(ServerHandler.USER)).getUsername(), file.getTo())) {
									if (file.getContentType() == ContentType.READY_FILE) {
										SendHandler.sendReadyFile(session, file);
									} else {
										SendHandler.sendTransmitFile(session, file);
									}
								}
							}
						}
					}
				}
			} catch (JMSException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
