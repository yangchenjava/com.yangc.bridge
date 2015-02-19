package com.yangc.bridge.comm.filter;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;

/**
 * @功能: 流量统计过滤器
 * @作者: yangc
 * @创建日期: 2014年8月26日 上午10:39:03
 */
public class TrafficCountFilter extends IoFilterAdapter {

	private static final String TRAFFIC_STATUS = "TRAFFIC_STATUS";

	@Override
	public void onPreAdd(IoFilterChain parent, String name, NextFilter nextFilter) throws Exception {
		if (parent.contains(this)) {
			throw new IllegalStateException("You can't add the same filter instance more than once. Create another instance and add it.");
		}
		parent.getSession().setAttribute(TRAFFIC_STATUS, new TrafficStatus());
	}

	@Override
	public void onPostRemove(IoFilterChain parent, String name, NextFilter nextFilter) throws Exception {
		parent.getSession().removeAttribute(TRAFFIC_STATUS);
	}

	@Override
	public void messageReceived(NextFilter nextFilter, IoSession session, Object message) throws Exception {
		if (message instanceof IoBuffer) {
			TrafficStatus trafficStatus = (TrafficStatus) session.getAttribute(TRAFFIC_STATUS);
			trafficStatus.readBytes += ((IoBuffer) message).remaining();
		}
		nextFilter.messageReceived(session, message);
	}

	@Override
	public void messageSent(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
		if (writeRequest.getMessage() instanceof IoBuffer) {
			TrafficStatus trafficStatus = (TrafficStatus) session.getAttribute(TRAFFIC_STATUS);
			trafficStatus.writeBytes += ((IoBuffer) writeRequest.getMessage()).remaining();
		}
		nextFilter.messageSent(session, writeRequest);
	}

	class TrafficStatus {
		long readBytes;
		long writeBytes;
	}

}
