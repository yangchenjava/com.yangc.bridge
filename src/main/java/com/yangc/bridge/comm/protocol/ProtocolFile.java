package com.yangc.bridge.comm.protocol;

/**
 * @功能: 68 [contentType(3)] [uuid] [fromLength] [toLength] [from] [to] 68 [fileNameLength] [fileName] [fileSize] [crc] 16
 * @功能: 68 [contentType(4)] [uuid] [fromLength] [toLength] [dataLength] [from] [to] 68 [fileNameLength] [fileName] [fileSize] [fileMd5] [offset] [data] [crc] 16
 * @作者: yangc
 * @创建日期: 2014年8月27日 下午9:50:57
 * @return
 */
public class ProtocolFile extends Protocol {

	private short fileNameLength; // 2byte
	private byte[] fileName;
	private long fileSize; // 8byte
	private byte[] fileMd5; // 32byte
	private int offset; // 4byte
	private byte[] data;

	public short getFileNameLength() {
		return fileNameLength;
	}

	public void setFileNameLength(short fileNameLength) {
		this.fileNameLength = fileNameLength;
	}

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
