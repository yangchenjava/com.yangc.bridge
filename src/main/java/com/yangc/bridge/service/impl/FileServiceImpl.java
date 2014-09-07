package com.yangc.bridge.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yangc.bridge.bean.TBridgeFile;
import com.yangc.bridge.service.FileService;
import com.yangc.dao.BaseDao;

@Service
@SuppressWarnings("unchecked")
public class FileServiceImpl implements FileService {

	@Autowired
	private BaseDao baseDao;

	@Override
	public void addFile(TBridgeFile file) {
		this.baseDao.save(file);
	}

	@Override
	public void delFile(Long id) {
		this.baseDao.delete(TBridgeFile.class, id);
	}

	@Override
	public List<TBridgeFile> getUnreceiveFileListByTo(String to) {
		String hql = "from TBridgeFile where to = ? order by id";
		return this.baseDao.findAll(hql, new Object[] { to });
	}

}
