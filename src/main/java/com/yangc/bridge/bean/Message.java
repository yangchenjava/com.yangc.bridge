package com.yangc.bridge.bean;

public class Message {

	private String id;
	private String from;
	private String to;
	private String type;
	private Protocol body;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Protocol getBody() {
		return body;
	}

	public void setBody(Protocol body) {
		this.body = body;
	}

}
