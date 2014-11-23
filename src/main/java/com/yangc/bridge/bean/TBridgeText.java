package com.yangc.bridge.bean;

public class TBridgeText extends TBridgeChat {

	private static final long serialVersionUID = 4407463520709519024L;

	private String data;

	public TBridgeText() {
		this.setChatType(0L);
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

}
