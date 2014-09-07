package com.yangc.bridge.service;

import java.util.List;

import com.yangc.bridge.bean.TBridgeChat;

public interface ChatService {

	public void addOrUpdateChat(TBridgeChat chat);

	public List<TBridgeChat> getUnreadChatListByTo(String to);

}
