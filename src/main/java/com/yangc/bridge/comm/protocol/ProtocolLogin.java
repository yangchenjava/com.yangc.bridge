package com.yangc.bridge.comm.protocol;

/**
 * @功能: 68 [contentType(1)] [uuid] 68 [usernameLength] [passwordLength] [username] [password] [crc] 16
 * @作者: yangc
 * @创建日期: 2014年8月27日 下午9:50:57
 * @return
 */
public class ProtocolLogin extends Protocol {

	private short usernameLength;
	private short passwordLength;
	private byte[] username;
	private byte[] password;

	public short getUsernameLength() {
		return usernameLength;
	}

	public void setUsernameLength(short usernameLength) {
		this.usernameLength = usernameLength;
	}

	public short getPasswordLength() {
		return passwordLength;
	}

	public void setPasswordLength(short passwordLength) {
		this.passwordLength = passwordLength;
	}

	public byte[] getUsername() {
		return username;
	}

	public void setUsername(byte[] username) {
		this.username = username;
	}

	public byte[] getPassword() {
		return password;
	}

	public void setPassword(byte[] password) {
		this.password = password;
	}

}
