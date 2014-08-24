package com.yangc.bridge.comm.codec;

import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;

import com.yangc.bridge.bean.TBridgeChat;
import com.yangc.bridge.comm.protocol.Protocol;

public class DecoderChat implements MessageDecoder {

	private Charset charset;

	public DecoderChat(Charset charset) {
		this.charset = charset;
	}

	@Override
	public MessageDecoderResult decodable(IoSession session, IoBuffer in) {
		if (in.remaining() < 44) {
			return NEED_DATA;
		}
		if (in.get() == Protocol.START_TAG) {
			if (in.get() == 2) {
				in.skip(36);
				short fromLength = in.getShort();
				short toLength = in.getShort();
				short dataLength = in.getShort();
				if (in.limit() >= 44 + fromLength + toLength + 1 + dataLength + 2) {
					if (in.skip(fromLength + toLength).get() == Protocol.END_TAG) {
						byte crc = 0;
						byte[] b = Arrays.copyOfRange(in.array(), 0, 44 + fromLength + toLength + 1 + dataLength);
						for (int i = 0; i < b.length; i++) {
							crc += b[i];
						}
						if (in.skip(dataLength).get() == crc && in.get() == Protocol.FINAL_TAG) {
							return OK;
						}
					}
				} else {
					return NEED_DATA;
				}
			}
		}
		return NOT_OK;
	}

	@Override
	public MessageDecoderResult decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		in.get(); // startTag
		in.get(); // contentType
		String uuid = in.getString(36, this.charset.newDecoder());
		short fromLength = in.getShort();
		short toLength = in.getShort();
		short dataLength = in.getShort();
		String from = in.getString(fromLength, this.charset.newDecoder());
		String to = in.getString(toLength, this.charset.newDecoder());
		in.get(); // endTag
		String data = in.getString(dataLength, this.charset.newDecoder());
		in.get(); // crc
		in.get(); // finalTag

		TBridgeChat chat = new TBridgeChat();
		chat.setUuid(uuid);
		chat.setFrom(from);
		chat.setTo(to);
		chat.setData(data);
		out.write(chat);
		return OK;
	}

	@Override
	public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
	}

}
