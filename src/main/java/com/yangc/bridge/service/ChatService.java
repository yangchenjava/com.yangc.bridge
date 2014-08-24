package com.yangc.bridge.service;

import java.util.List;

import com.yangc.bridge.bean.TBridgeChat;

public interface ChatService {

	public void addOrUpdateChat(Long id, String uuid, String from, String to, String data, Long status);

	public void updateChatStatus(Long[] ids);

	public List<TBridgeChat> getUnreadChatListByTo(String to);

}
