package com.yangc.bridge.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yangc.bridge.bean.TBridgeFile;
import com.yangc.bridge.service.FileService;
import com.yangc.dao.BaseDao;
import com.yangc.dao.JdbcDao;

@Service
@SuppressWarnings("unchecked")
public class FileServiceImpl implements FileService {

	@Autowired
	private BaseDao baseDao;
	@Autowired
	private JdbcDao jdbcDao;

	@Override
	public void addFile(TBridgeFile file) {
		this.baseDao.save(file);
	}

	@Override
	public void delFiles(List<Long> ids) {
		String sql = JdbcDao.SQL_MAPPING.get("bridge.file.delFiles");
		List<Object[]> paramList = new ArrayList<Object[]>();
		for (Long id : ids) {
			paramList.add(new Object[] { id });
		}
		this.jdbcDao.batchExecute(sql, paramList);
	}

	@Override
	public List<TBridgeFile> getUnreceiveFileListByTo(String to) {
		String hql = "from TBridgeFile where to = ? order by id";
		return this.baseDao.findAll(hql, new Object[] { to });
	}

}
