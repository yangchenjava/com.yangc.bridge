package com.yangc.bridge.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yangc.bridge.bean.TBridgeChat;
import com.yangc.bridge.service.ChatService;
import com.yangc.dao.BaseDao;

@Service
@SuppressWarnings("unchecked")
public class ChatServiceImpl implements ChatService {

	@Autowired
	private BaseDao baseDao;

	@Override
	public void addOrUpdateChat(TBridgeChat chat) {
		this.baseDao.saveOrUpdate(chat);
	}

	@Override
	public List<TBridgeChat> getUnreadChatListByTo(String to) {
		String hql = "from TBridgeChat where to = ? and status = 0 order by id";
		return this.baseDao.findAll(hql, new Object[] { to });
	}

}
