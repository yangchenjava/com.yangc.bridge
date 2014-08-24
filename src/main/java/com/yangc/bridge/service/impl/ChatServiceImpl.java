package com.yangc.bridge.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yangc.bridge.bean.TBridgeChat;
import com.yangc.bridge.service.ChatService;
import com.yangc.dao.BaseDao;
import com.yangc.dao.JdbcDao;

@Service
@SuppressWarnings("unchecked")
public class ChatServiceImpl implements ChatService {

	@Autowired
	private BaseDao baseDao;
	@Autowired
	private JdbcDao jdbcDao;

	@Override
	public void addOrUpdateChat(Long id, String uuid, String from, String to, String data, Long status) {
		TBridgeChat chat = (TBridgeChat) this.baseDao.get(TBridgeChat.class, id);
		if (chat == null) {
			chat = new TBridgeChat();
		}
		chat.setUuid(uuid);
		chat.setFrom(from);
		chat.setTo(to);
		chat.setData(data);
		chat.setStatus(status);
		this.baseDao.save(chat);
	}

	@Override
	public void updateChatStatus(Long[] ids) {
		String sql = JdbcDao.SQL_MAPPING.get("bridge.chat.updateChatStatus");
		List<Object[]> paramList = new ArrayList<Object[]>();
		for (Long id : ids) {
			paramList.add(new Object[] { id });
		}
		this.jdbcDao.batchExecute(sql, paramList);
	}

	@Override
	public List<TBridgeChat> getUnreadChatListByTo(String to) {
		String hql = "from TBridgeChat where to = ? and status = 0 order by id";
		return this.baseDao.findAll(hql, new Object[] { to });
	}

}
