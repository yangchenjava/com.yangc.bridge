package com.yangc.bridge.bean;

import com.yangc.bean.BaseBean;

public class TBridgeFile extends BaseBean {

	private static final long serialVersionUID = 4407463520709519024L;

	private String uuid;
	private String from;
	private String to;
	private String fileName;
	private Long fileSize;
	private String fileMd5;

	private byte contentType;
	private byte transmitStatus;
	private int offset;
	private byte[] data;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Long getFileSize() {
		return fileSize;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	public String getFileMd5() {
		return fileMd5;
	}

	public void setFileMd5(String fileMd5) {
		this.fileMd5 = fileMd5;
	}

	public byte getContentType() {
		return contentType;
	}

	public void setContentType(byte contentType) {
		this.contentType = contentType;
	}

	public byte getTransmitStatus() {
		return transmitStatus;
	}

	public void setTransmitStatus(byte transmitStatus) {
		this.transmitStatus = transmitStatus;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

}
