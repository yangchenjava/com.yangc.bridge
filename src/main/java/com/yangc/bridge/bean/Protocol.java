package com.yangc.bridge.bean;

public class Protocol {

	private byte startTag;
	private short dataLength;
	private byte endTag;
	private byte finalTag;

	public byte getStartTag() {
		return startTag;
	}

	public void setStartTag(byte startTag) {
		this.startTag = startTag;
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

	public void setEndTag(byte endTag) {
		this.endTag = endTag;
	}

	public byte getFinalTag() {
		return finalTag;
	}

	public void setFinalTag(byte finalTag) {
		this.finalTag = finalTag;
	}

}
