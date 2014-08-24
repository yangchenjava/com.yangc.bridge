package com.yangc.bridge.bean;

import com.yangc.bean.BaseBean;

public class TBridgeChat extends BaseBean {

	private static final long serialVersionUID = -8248931483988118867L;

	private String uuid;
	private String from;
	private String to;
	private String data;
	private Long status;

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

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public Long getStatus() {
		return status;
	}

	public void setStatus(Long status) {
		this.status = status;
	}

}
