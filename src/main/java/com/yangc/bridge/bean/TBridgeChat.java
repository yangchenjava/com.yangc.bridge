package com.yangc.bridge.bean;

import com.yangc.bean.BaseBean;

public class TBridgeChat extends BaseBean {

	private static final long serialVersionUID = -8248931483988118867L;

	private String uuid;
	private String from;
	private String to;
	private Long chatType;
	private Long status = 0L;

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

	public Long getChatType() {
		return chatType;
	}

	public void setChatType(Long chatType) {
		this.chatType = chatType;
	}

	public Long getStatus() {
		return status;
	}

	public void setStatus(Long status) {
		this.status = status;
	}

}
