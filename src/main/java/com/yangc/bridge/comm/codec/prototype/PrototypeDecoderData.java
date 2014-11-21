package com.yangc.bridge.comm.codec.prototype;

import java.nio.charset.CharsetDecoder;
import java.util.Arrays;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import com.yangc.bridge.bean.ResultBean;
import com.yangc.bridge.bean.TBridgeChat;
import com.yangc.bridge.bean.TBridgeFile;
import com.yangc.bridge.bean.UserBean;
import com.yangc.bridge.comm.protocol.ContentType;
import com.yangc.bridge.comm.protocol.Tag;

public class PrototypeDecoderData extends CumulativeProtocolDecoder {

	private CharsetDecoder charsetDecoder;

	public PrototypeDecoderData(CharsetDecoder charsetDecoder) {
		this.charsetDecoder = charsetDecoder;
	}

	@Override
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		if (in.hasRemaining()) {
			in.mark();
			if (in.remaining() < 2) {
				in.reset();
				return false;
			}
			int position = in.position();
			if (in.get() == Tag.START) {
				byte contentType = in.get();
				if (contentType == ContentType.HEART) {
					if (!this.decodeHeart(position, in, out)) {
						in.reset();
						return false;
					}
				} else if (contentType == ContentType.RESULT) {
					if (!this.decodeResult(position, in, out)) {
						in.reset();
						return false;
					}
				} else if (contentType == ContentType.LOGIN) {
					if (!this.decodeLogin(position, in, out)) {
						in.reset();
						return false;
					}
				} else if (contentType == ContentType.CHAT) {
					if (!this.decodeChat(position, in, out)) {
						in.reset();
						return false;
					}
				} else if (contentType == ContentType.READY_FILE) {
					if (!this.decodeReadyFile(position, in, out)) {
						in.reset();
						return false;
					}
				} else if (contentType == ContentType.TRANSMIT_FILE) {
					if (!this.decodeTransmitFile(position, in, out)) {
						in.reset();
						return false;
					}
				}
			}
			if (in.hasRemaining()) {
				return true;
			}
		}
		return false;
	}

	private boolean decodeHeart(int position, IoBuffer in, ProtocolDecoderOutput out) {
		if (in.remaining() < 1) {
			return false;
		}
		if (in.get() == Tag.END) {
			if (in.remaining() >= 2) {
				byte crc = 0;
				byte[] b = Arrays.copyOfRange(in.array(), position, position + 3);
				for (int i = 0; i < b.length; i++) {
					crc += b[i];
				}
				if (in.get() == crc && in.get() == Tag.FINAL) {
					out.write(ContentType.HEART);
				}
			} else {
				return false;
			}
		}
		return true;
	}

	private boolean decodeResult(int position, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		if (in.remaining() < 44) {
			return false;
		}
		String uuid = in.getString(36, this.charsetDecoder);
		short fromLength = in.getShort();
		short toLength = in.getShort();
		int dataLength = in.getInt();
		if (in.remaining() >= fromLength + toLength + 1 + dataLength + 2) {
			String from = in.getString(fromLength, this.charsetDecoder);
			String to = in.getString(toLength, this.charsetDecoder);
			if (in.get() == Tag.END) {
				byte success = in.get();
				String data = in.getString(dataLength - 1, this.charsetDecoder);

				byte crc = 0;
				byte[] b = Arrays.copyOfRange(in.array(), position, position + 46 + fromLength + toLength + 1 + dataLength);
				for (int i = 0; i < b.length; i++) {
					crc += b[i];
				}
				if (in.get() == crc && in.get() == Tag.FINAL) {
					ResultBean result = new ResultBean();
					result.setUuid(uuid);
					result.setFrom(from);
					result.setTo(to);
					result.setSuccess(success == 0 ? false : true);
					result.setData(data);
					out.write(result);
				}
			}
		} else {
			return false;
		}
		return true;
	}

	private boolean decodeLogin(int position, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		if (in.remaining() < 41) {
			return false;
		}
		String uuid = in.getString(36, this.charsetDecoder);
		if (in.get() == Tag.END) {
			short usernameLength = in.getShort();
			short passwordLength = in.getShort();
			if (in.remaining() >= usernameLength + passwordLength + 2) {
				String username = in.getString(usernameLength, this.charsetDecoder);
				String password = in.getString(passwordLength, this.charsetDecoder);

				byte crc = 0;
				byte[] b = Arrays.copyOfRange(in.array(), position, position + 43 + usernameLength + passwordLength);
				for (int i = 0; i < b.length; i++) {
					crc += b[i];
				}
				if (in.get() == crc && in.get() == Tag.FINAL) {
					UserBean user = new UserBean();
					user.setUuid(uuid);
					user.setUsername(username);
					user.setPassword(password);
					out.write(user);
				}
			} else {
				return false;
			}
		}
		return true;
	}

	private boolean decodeChat(int position, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		if (in.remaining() < 44) {
			return false;
		}
		String uuid = in.getString(36, this.charsetDecoder);
		short fromLength = in.getShort();
		short toLength = in.getShort();
		int dataLength = in.getInt();
		if (in.remaining() >= fromLength + toLength + 1 + dataLength + 2) {
			String from = in.getString(fromLength, this.charsetDecoder);
			String to = in.getString(toLength, this.charsetDecoder);
			if (in.get() == Tag.END) {
				String data = in.getString(dataLength, this.charsetDecoder);

				byte crc = 0;
				byte[] b = Arrays.copyOfRange(in.array(), position, position + 46 + fromLength + toLength + 1 + dataLength);
				for (int i = 0; i < b.length; i++) {
					crc += b[i];
				}
				if (in.get() == crc && in.get() == Tag.FINAL) {
					TBridgeChat chat = new TBridgeChat();
					chat.setUuid(uuid);
					chat.setFrom(from);
					chat.setTo(to);
					chat.setData(data);
					out.write(chat);
				}
			}
		} else {
			return false;
		}
		return true;
	}

	private boolean decodeReadyFile(int position, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		if (in.remaining() < 40) {
			return false;
		}
		String uuid = in.getString(36, this.charsetDecoder);
		short fromLength = in.getShort();
		short toLength = in.getShort();
		if (in.remaining() >= fromLength + toLength + 3) {
			String from = in.getString(fromLength, this.charsetDecoder);
			String to = in.getString(toLength, this.charsetDecoder);
			if (in.get() == Tag.END) {
				short fileNameLength = in.getShort();
				if (in.remaining() >= fileNameLength + 10) {
					String fileName = in.getString(fileNameLength, this.charsetDecoder);
					long fileSize = in.getLong();

					byte crc = 0;
					byte[] b = Arrays.copyOfRange(in.array(), position, position + 42 + fromLength + toLength + 3 + fileNameLength + 8);
					for (int i = 0; i < b.length; i++) {
						crc += b[i];
					}
					if (in.get() == crc && in.get() == Tag.FINAL) {
						TBridgeFile file = new TBridgeFile();
						file.setContentType(ContentType.READY_FILE);
						file.setUuid(uuid);
						file.setFrom(from);
						file.setTo(to);
						file.setFileName(fileName);
						file.setFileSize(fileSize);
						out.write(file);
					}
				} else {
					return false;
				}
			}
		} else {
			return false;
		}
		return true;
	}

	private boolean decodeTransmitFile(int position, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		if (in.remaining() < 44) {
			return false;
		}
		String uuid = in.getString(36, this.charsetDecoder);
		short fromLength = in.getShort();
		short toLength = in.getShort();
		int dataLength = in.getInt();
		if (in.remaining() >= fromLength + toLength + 1 + dataLength + 2) {
			String from = in.getString(fromLength, this.charsetDecoder);
			String to = in.getString(toLength, this.charsetDecoder);
			if (in.get() == Tag.END) {
				short fileNameLength = in.getShort();
				String fileName = in.getString(fileNameLength, this.charsetDecoder);
				long fileSize = in.getLong();
				String fileMd5 = in.getString(32, this.charsetDecoder);
				int offset = in.getInt();
				byte[] data = new byte[dataLength - fileNameLength - 46];
				in.get(data);

				byte crc = 0;
				byte[] b = Arrays.copyOfRange(in.array(), position, position + 46 + fromLength + toLength + 1 + dataLength);
				for (int i = 0; i < b.length; i++) {
					crc += b[i];
				}
				if (in.get() == crc && in.get() == Tag.FINAL) {
					TBridgeFile file = new TBridgeFile();
					file.setContentType(ContentType.TRANSMIT_FILE);
					file.setUuid(uuid);
					file.setFrom(from);
					file.setTo(to);
					file.setFileName(fileName);
					file.setFileSize(fileSize);
					file.setFileMd5(fileMd5);
					file.setOffset(offset);
					file.setData(data);
					out.write(file);
				}
			}
		} else {
			return false;
		}
		return true;
	}

}
