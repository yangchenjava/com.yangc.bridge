package com.yangc.bridge.comm.protocol;

public class Protocol {

	public static final byte START_TAG = 0x68; // 1byte
	private byte contentType; // 1byte 内容类型 0:result, 1:login, 2:chat, 3:file
	private byte[] uuid; // 36byte
	private short fromLength; // 2byte
	private short toLength; // 2byte
	private short dataLength; // 2byte
	private byte[] from;
	private byte[] to;
	public static final byte END_TAG = 0x68; // 1byte

	public static final byte FINAL_TAG = 0x16; // 1byte

	public byte getContentType() {
		return contentType;
	}

	public void setContentType(byte contentType) {
		this.contentType = contentType;
	}

	public byte[] getUuid() {
		return uuid;
	}

	public void setUuid(byte[] uuid) {
		this.uuid = uuid;
	}

	public short getFromLength() {
		return fromLength;
	}

	public void setFromLength(short fromLength) {
		this.fromLength = fromLength;
	}

	public short getToLength() {
		return toLength;
	}

	public void setToLength(short toLength) {
		this.toLength = toLength;
	}

	public short getDataLength() {
		return dataLength;
	}

	public void setDataLength(short dataLength) {
		this.dataLength = dataLength;
	}

	public byte[] getFrom() {
		return from;
	}

	public void setFrom(byte[] from) {
		this.from = from;
	}

	public byte[] getTo() {
		return to;
	}

	public void setTo(byte[] to) {
		this.to = to;
	}

}
