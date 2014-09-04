package com.yangc.bridge.service;

import java.util.List;

import com.yangc.bridge.bean.TBridgeFile;

public interface FileService {

	public void addFile(TBridgeFile file);

	public void delFiles(List<Long> ids);

	public List<TBridgeFile> getUnreceiveFileListByTo(String to);

}
