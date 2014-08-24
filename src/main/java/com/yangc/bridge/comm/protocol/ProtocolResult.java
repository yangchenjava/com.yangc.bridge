package com.yangc.bridge.comm.protocol;

public class ProtocolResult extends Protocol {

	private byte success;
	private byte[] message;

	public byte getSuccess() {
		return success;
	}

	public void setSuccess(byte success) {
		this.success = success;
	}

	public byte[] getMessage() {
		return message;
	}

	public void setMessage(byte[] message) {
		this.message = message;
	}

}
