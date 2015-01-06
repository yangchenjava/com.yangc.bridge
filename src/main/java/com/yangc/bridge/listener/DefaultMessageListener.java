package com.yangc.bridge.listener;

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.yangc.bridge.bean.TBridgeChat;
import com.yangc.bridge.bean.TBridgeFile;
import com.yangc.bridge.bean.UserBean;
import com.yangc.bridge.comm.Server;

public class DefaultMessageListener implements MessageListener {

	private static final Logger logger = Logger.getLogger(DefaultMessageListener.class);

	@Autowired
	private Server server;

	@Override
	public void onMessage(Message message) {
		if (message != null) {
			try {
				if (message instanceof TextMessage) {
					logger.info("MessageListener - Text=" + ((TextMessage) message).getText());
				} else if (message instanceof ObjectMessage) {
					Serializable obj = ((ObjectMessage) message).getObject();
					if (obj instanceof UserBean) {
						UserBean user = (UserBean) obj;
					} else if (obj instanceof TBridgeChat) {
						TBridgeChat chat = (TBridgeChat) obj;
					} else if (obj instanceof TBridgeFile) {
						TBridgeFile file = (TBridgeFile) obj;
					}
				}
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}

}
