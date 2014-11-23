package com.yangc.bridge.service;

import java.util.List;

import com.yangc.bridge.bean.TBridgeChat;
import com.yangc.bridge.bean.TBridgeFile;
import com.yangc.bridge.bean.TBridgeText;

public interface ChatService {

	public void addChat(TBridgeChat chat);

	public void addText(TBridgeText text);

	public void addFile(TBridgeFile file);

	public void updateChatStatusByUuid(String uuid);

	public List<TBridgeChat> getUnreadChatListByTo(String to);

}
