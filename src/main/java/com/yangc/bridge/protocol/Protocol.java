package com.yangc.bridge.protocol;

public class Protocol {

	public static final byte START_TAG = 0x68; // 1byte
	private short headLength; // 2byte
	private short bodyLength; // 2byte
	public static final byte END_TAG = 0x68; // 1byte
	private byte[] uuid; // 36byte
	private byte contentType; // 1byte 内容类型 0:chat 1:file
	private short fromLength; // 2byte
	private short toLength; // 2byte
	private byte[] from;
	private byte[] to;

	private byte crc; // 1byte
	public static final byte FINAL_TAG = 0x16; // 1byte

	public short getHeadLength() {
		return headLength;
	}

	public void setHeadLength(short headLength) {
		this.headLength = headLength;
	}

	public short getBodyLength() {
		return bodyLength;
	}

	public void setBodyLength(short bodyLength) {
		this.bodyLength = bodyLength;
	}

	public byte[] getUuid() {
		return uuid;
	}

	public void setUuid(byte[] uuid) {
		this.uuid = uuid;
	}

	public byte getContentType() {
		return contentType;
	}

	public void setContentType(byte contentType) {
		this.contentType = contentType;
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

	public byte getCrc() {
		return crc;
	}

	public void setCrc(byte crc) {
		this.crc = crc;
	}

}
