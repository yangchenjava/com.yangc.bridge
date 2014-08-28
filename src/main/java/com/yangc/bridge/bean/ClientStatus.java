package com.yangc.bridge.bean;

public class ClientStatus {

	private String username;
	private String ipAddress;
	private long sessionId;
	private String lastIoTime;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public long getSessionId() {
		return sessionId;
	}

	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}

	public String getLastIoTime() {
		return lastIoTime;
	}

	public void setLastIoTime(String lastIoTime) {
		this.lastIoTime = lastIoTime;
	}

}
