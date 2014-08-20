package com.yangc.bridge.bean;

public class ProtocolChat extends Protocol {

	private byte[] uuid; // 36byte
	private byte all; // 1byte
	private byte part; // 1byte
	private byte[] data;

	public byte[] getUuid() {
		return uuid;
	}

	public void setUuid(byte[] uuid) {
		this.uuid = uuid;
	}

	public byte getAll() {
		return all;
	}

	public void setAll(byte all) {
		this.all = all;
	}

	public byte getPart() {
		return part;
	}

	public void setPart(byte part) {
		this.part = part;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

}
