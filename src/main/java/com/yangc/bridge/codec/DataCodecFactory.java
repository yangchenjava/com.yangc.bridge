package com.yangc.bridge.codec;

import java.nio.charset.Charset;

import org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory;

import com.yangc.bridge.protocol.Protocol;

public class DataCodecFactory extends DemuxingProtocolCodecFactory {

	public DataCodecFactory() {
		this("UTF-8");
	}

	public DataCodecFactory(String charsetName) {
		this.addMessageEncoder(Protocol.class, new DataEncoder());
		this.addMessageDecoder(new DataDecoder(Charset.forName(charsetName)));
	}

}
