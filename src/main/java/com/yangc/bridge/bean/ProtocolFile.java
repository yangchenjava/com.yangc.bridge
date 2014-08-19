package com.yangc.bridge.bean;

public class ProtocolFile extends Protocol {

	private byte[] fileName;
	private long fileSize;
	private byte[] fileMd5;
	private int offset;
	private byte[] data;

	public byte[] getFileName() {
		return fileName;
	}

	public void setFileName(byte[] fileName) {
		this.fileName = fileName;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public byte[] getFileMd5() {
		return fileMd5;
	}

	public void setFileMd5(byte[] fileMd5) {
		this.fileMd5 = fileMd5;
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
