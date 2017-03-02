package com.yangc.bridge.comm.codec.protobuf;

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
import com.yangc.bridge.comm.protocol.protobuf.ProtobufMessage;

public class ProtobufDecoderData extends CumulativeProtocolDecoder {

	@Override
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		if (in.hasRemaining()) {
			if (in.remaining() < 2) {
				return false;
			}
			in.mark();
			int position = in.position();
			if (in.get() == Tag.START) {
				switch (in.get()) {
				case ContentType.HEART:
					if (!this.decodeHeart(position, in, out)) {
						in.reset();
						return false;
					}
					break;
				case ContentType.RESULT:
					if (!this.decodeResult(position, in, out)) {
						in.reset();
						return false;
					}
					break;
				case ContentType.LOGIN:
					if (!this.decodeLogin(position, in, out)) {
						in.reset();
						return false;
					}
					break;
				case ContentType.CHAT:
					if (!this.decodeChat(position, in, out)) {
						in.reset();
						return false;
					}
					break;
				case ContentType.READY_FILE:
					if (!this.decodeReadyFile(position, in, out)) {
						in.reset();
						return false;
					}
					break;
				case ContentType.TRANSMIT_FILE:
					if (!this.decodeTransmitFile(position, in, out)) {
						in.reset();
						return false;
					}
					break;
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
		if (in.remaining() < 4) {
			return false;
		}
		int serializedSize = in.getInt();
		if (in.remaining() >= serializedSize + 2) {
			byte[] data = new byte[serializedSize];
			in.get(data);
			ProtobufMessage.Result message = ProtobufMessage.Result.parseFrom(data);

			byte crc = 0;
			byte[] b = Arrays.copyOfRange(in.array(), position, position + 6 + serializedSize);
			for (int i = 0; i < b.length; i++) {
				crc += b[i];
			}
			if (in.get() == crc && in.get() == Tag.FINAL) {
				ResultBean result = new ResultBean();
				result.setUuid(message.getUuid());
				result.setSuccess(message.getSuccess());
				result.setData(message.getData());
				out.write(result);
			}
		} else {
			return false;
		}
		return true;
	}

	private boolean decodeLogin(int position, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		if (in.remaining() < 4) {
			return false;
		}
		int serializedSize = in.getInt();
		if (in.remaining() >= serializedSize + 2) {
			byte[] data = new byte[serializedSize];
			in.get(data);
			ProtobufMessage.Login message = ProtobufMessage.Login.parseFrom(data);

			byte crc = 0;
			byte[] b = Arrays.copyOfRange(in.array(), position, position + 6 + serializedSize);
			for (int i = 0; i < b.length; i++) {
				crc += b[i];
			}
			if (in.get() == crc && in.get() == Tag.FINAL) {
				UserBean user = new UserBean();
				user.setUuid(message.getUuid());
				user.setUsername(message.getUsername());
				user.setPassword(message.getPassword());
				out.write(user);
			}
		} else {
			return false;
		}
		return true;
	}

	private boolean decodeChat(int position, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		if (in.remaining() < 4) {
			return false;
		}
		int serializedSize = in.getInt();
		if (in.remaining() >= serializedSize + 2) {
			byte[] data = new byte[serializedSize];
			in.get(data);
			ProtobufMessage.Chat message = ProtobufMessage.Chat.parseFrom(data);

			byte crc = 0;
			byte[] b = Arrays.copyOfRange(in.array(), position, position + 6 + serializedSize);
			for (int i = 0; i < b.length; i++) {
				crc += b[i];
			}
			if (in.get() == crc && in.get() == Tag.FINAL) {
				TBridgeChat chat = new TBridgeChat();
				chat.setUuid(message.getUuid());
				chat.setFrom(message.getFrom());
				chat.setTo(message.getTo());
				chat.setData(message.getData());
				out.write(chat);
			}
		} else {
			return false;
		}
		return true;
	}

	private boolean decodeReadyFile(int position, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		if (in.remaining() < 4) {
			return false;
		}
		int serializedSize = in.getInt();
		if (in.remaining() >= serializedSize + 2) {
			byte[] data = new byte[serializedSize];
			in.get(data);
			ProtobufMessage.File message = ProtobufMessage.File.parseFrom(data);

			byte crc = 0;
			byte[] b = Arrays.copyOfRange(in.array(), position, position + 6 + serializedSize);
			for (int i = 0; i < b.length; i++) {
				crc += b[i];
			}
			if (in.get() == crc && in.get() == Tag.FINAL) {
				TBridgeFile file = new TBridgeFile();
				file.setContentType(ContentType.READY_FILE);
				file.setUuid(message.getUuid());
				file.setFrom(message.getFrom());
				file.setTo(message.getTo());
				file.setFileName(message.getFileName());
				file.setFileSize(message.getFileSize());
				out.write(file);
			}
		} else {
			return false;
		}
		return true;
	}

	private boolean decodeTransmitFile(int position, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		if (in.remaining() < 4) {
			return false;
		}
		int serializedSize = in.getInt();
		if (in.remaining() >= serializedSize + 2) {
			byte[] data = new byte[serializedSize];
			in.get(data);
			ProtobufMessage.File message = ProtobufMessage.File.parseFrom(data);

			byte crc = 0;
			byte[] b = Arrays.copyOfRange(in.array(), position, position + 6 + serializedSize);
			for (int i = 0; i < b.length; i++) {
				crc += b[i];
			}
			if (in.get() == crc && in.get() == Tag.FINAL) {
				TBridgeFile file = new TBridgeFile();
				file.setContentType(ContentType.TRANSMIT_FILE);
				file.setUuid(message.getUuid());
				file.setFrom(message.getFrom());
				file.setTo(message.getTo());
				file.setFileName(message.getFileName());
				file.setFileSize(message.getFileSize());
				file.setFileMd5(message.getFileMd5());
				file.setOffset(message.getOffset());
				file.setData(message.getData().toByteArray());
				out.write(file);
			}
		} else {
			return false;
		}
		return true;
	}

}
