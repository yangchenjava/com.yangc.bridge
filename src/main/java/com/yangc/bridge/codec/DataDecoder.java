package com.yangc.bridge.codec;

import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;

import com.yangc.bridge.bean.ChatBean;
import com.yangc.bridge.protocol.Protocol;

public class DataDecoder implements MessageDecoder {

	private Charset charset;

	public DataDecoder(Charset charset) {
		this.charset = charset;
	}

	@Override
	public MessageDecoderResult decodable(IoSession session, IoBuffer in) {
		if (in.remaining() < 6) {
			return NEED_DATA;
		}
		if (in.get() == Protocol.START_TAG) {
			short headLength = in.getShort();
			short bodyLength = in.getShort();
			if (in.get() == Protocol.END_TAG) {
				if (in.limit() >= headLength + bodyLength) {
					if (in.get(36) == 0 && in.get(headLength + bodyLength - 1) == Protocol.FINAL_TAG) {
						return OK;
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
		in.getShort(); // headLength
		short bodyLength = in.getShort();
		in.get(); // endTag

		String uuid = in.getString(36, this.charset.newDecoder());

		in.get(); // contentType
		short fromLength = in.getShort();
		short toLength = in.getShort();

		String from = in.getString(fromLength, this.charset.newDecoder());
		String to = in.getString(toLength, this.charset.newDecoder());
		String data = in.getString(bodyLength, this.charset.newDecoder());

		ChatBean chatBean = new ChatBean();
		chatBean.setUuid(uuid);
		chatBean.setFrom(from);
		chatBean.setTo(to);
		chatBean.setData(data);
		out.write(chatBean);
		return OK;
	}

	@Override
	public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {

	}

}
