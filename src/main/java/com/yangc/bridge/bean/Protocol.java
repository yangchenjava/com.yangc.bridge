package com.yangc.bridge.bean;

public class Protocol {

	private final byte startTag = 0x68; // 1byte
	private byte type; // 1byte 内容类型 0:chat 1:file
	private short dataLength; // 2byte
	private final byte endTag = 0x68; // 1byte
	private final byte finalTag = 0x16; // 1byte

	public byte getStartTag() {
		return startTag;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public short getDataLength() {
		return dataLength;
	}

	public void setDataLength(short dataLength) {
		this.dataLength = dataLength;
	}

	public byte getEndTag() {
		return endTag;
	}

	public byte getFinalTag() {
		return finalTag;
	}

}
