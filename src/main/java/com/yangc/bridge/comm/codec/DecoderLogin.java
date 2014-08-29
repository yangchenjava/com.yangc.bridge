package com.yangc.bridge.comm.codec;

import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;

import com.yangc.bridge.bean.UserBean;
import com.yangc.bridge.comm.protocol.ContentType;
import com.yangc.bridge.comm.protocol.Protocol;

public class DecoderLogin implements MessageDecoder {

	private Charset charset;

	public DecoderLogin(Charset charset) {
		this.charset = charset;
	}

	@Override
	public MessageDecoderResult decodable(IoSession session, IoBuffer in) {
		if (in.remaining() < 43) {
			return NEED_DATA;
		}
		if (in.get() == Protocol.START_TAG && in.get() == ContentType.LOGIN) {
			in.skip(36);
			if (in.get() == Protocol.END_TAG) {
				short usernameLength = in.getShort();
				short passwordLength = in.getShort();
				if (in.limit() >= 43 + usernameLength + passwordLength + 2) {
					byte crc = 0;
					byte[] b = Arrays.copyOfRange(in.array(), 0, 43 + usernameLength + passwordLength);
					for (int i = 0; i < b.length; i++) {
						crc += b[i];
					}
					if (in.skip(usernameLength + passwordLength).get() == crc && in.get() == Protocol.FINAL_TAG) {
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
		in.get(); // contentType
		String uuid = in.getString(36, this.charset.newDecoder());
		in.get(); // endTag
		short usernameLength = in.getShort();
		short passwordLength = in.getShort();
		String username = in.getString(usernameLength, this.charset.newDecoder());
		String password = in.getString(passwordLength, this.charset.newDecoder());
		in.get(); // crc
		in.get(); // finalTag

		UserBean user = new UserBean();
		user.setUuid(uuid);
		user.setUsername(username);
		user.setPassword(password);
		out.write(user);
		return OK;
	}

	@Override
	public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
	}

}
