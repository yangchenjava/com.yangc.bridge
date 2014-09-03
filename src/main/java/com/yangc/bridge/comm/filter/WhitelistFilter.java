package com.yangc.bridge.comm.filter;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.filter.firewall.Subnet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @功能: 白名单过滤器
 * @作者: yangc
 * @创建日期: 2014年8月26日 上午10:39:03
 */
public class WhitelistFilter extends IoFilterAdapter {

	private final List<Subnet> whitelist = new CopyOnWriteArrayList<Subnet>();

	private static final Logger LOGGER = LoggerFactory.getLogger(WhitelistFilter.class);

	/**
	 * Sets the addresses to be whitelisted.
	 * 
	 * NOTE: this call will remove any previously whitelisted addresses.
	 * 
	 * @param addresses an array of addresses to be whitelisted.
	 */
	public void setWhitelist(InetAddress[] addresses) {
		if (addresses == null) {
			throw new IllegalArgumentException("addresses");
		}
		whitelist.clear();
		for (int i = 0; i < addresses.length; i++) {
			InetAddress addr = addresses[i];
			block(addr);
		}
	}

	/**
	 * Sets the subnets to be whitelisted.
	 * 
	 * NOTE: this call will remove any previously whitelisted subnets.
	 * 
	 * @param subnets an array of subnets to be whitelisted.
	 */
	public void setSubnetWhitelist(Subnet[] subnets) {
		if (subnets == null) {
			throw new IllegalArgumentException("Subnets must not be null");
		}
		whitelist.clear();
		for (Subnet subnet : subnets) {
			block(subnet);
		}
	}

	/**
	 * Sets the addresses to be whitelisted.
	 * 
	 * NOTE: this call will remove any previously whitelisted addresses.
	 * 
	 * @param addresses a collection of InetAddress objects representing the addresses to be whitelisted.
	 * @throws IllegalArgumentException if the specified collections contains non-{@link InetAddress} objects.
	 */
	public void setWhitelist(Iterable<InetAddress> addresses) {
		if (addresses == null) {
			throw new IllegalArgumentException("addresses");
		}
		whitelist.clear();
		for (InetAddress address : addresses) {
			block(address);
		}
	}

	/**
	 * Sets the subnets to be whitelisted.
	 * 
	 * NOTE: this call will remove any previously whitelisted subnets.
	 * 
	 * @param subnets an array of subnets to be whitelisted.
	 */
	public void setSubnetWhitelist(Iterable<Subnet> subnets) {
		if (subnets == null) {
			throw new IllegalArgumentException("Subnets must not be null");
		}
		whitelist.clear();
		for (Subnet subnet : subnets) {
			block(subnet);
		}
	}

	/**
	 * Blocks the specified endpoint.
	 */
	public void block(InetAddress address) {
		if (address == null) {
			throw new IllegalArgumentException("Adress to block can not be null");
		}
		block(new Subnet(address, 32));
	}

	/**
	 * Blocks the specified subnet.
	 */
	public void block(Subnet subnet) {
		if (subnet == null) {
			throw new IllegalArgumentException("Subnet can not be null");
		}
		whitelist.add(subnet);
	}

	/**
	 * Unblocks the specified endpoint.
	 */
	public void unblock(InetAddress address) {
		if (address == null) {
			throw new IllegalArgumentException("Adress to unblock can not be null");
		}
		unblock(new Subnet(address, 32));
	}

	/**
	 * Unblocks the specified subnet.
	 */
	public void unblock(Subnet subnet) {
		if (subnet == null) {
			throw new IllegalArgumentException("Subnet can not be null");
		}
		whitelist.remove(subnet);
	}

	@Override
	public void sessionCreated(NextFilter nextFilter, IoSession session) {
		if (!isBlocked(session)) {
			// forward if not blocked
			nextFilter.sessionCreated(session);
		} else {
			blockSession(session);
		}
	}

	@Override
	public void sessionOpened(NextFilter nextFilter, IoSession session) throws Exception {
		if (!isBlocked(session)) {
			// forward if not blocked
			nextFilter.sessionOpened(session);
		} else {
			blockSession(session);
		}
	}

	@Override
	public void sessionClosed(NextFilter nextFilter, IoSession session) throws Exception {
		if (!isBlocked(session)) {
			// forward if not blocked
			nextFilter.sessionClosed(session);
		} else {
			blockSession(session);
		}
	}

	@Override
	public void sessionIdle(NextFilter nextFilter, IoSession session, IdleStatus status) throws Exception {
		if (!isBlocked(session)) {
			// forward if not blocked
			nextFilter.sessionIdle(session, status);
		} else {
			blockSession(session);
		}
	}

	@Override
	public void messageReceived(NextFilter nextFilter, IoSession session, Object message) {
		if (!isBlocked(session)) {
			// forward if not blocked
			nextFilter.messageReceived(session, message);
		} else {
			blockSession(session);
		}
	}

	@Override
	public void messageSent(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
		if (!isBlocked(session)) {
			// forward if not blocked
			nextFilter.messageSent(session, writeRequest);
		} else {
			blockSession(session);
		}
	}

	private void blockSession(IoSession session) {
		LOGGER.warn("Remote address not in the whitelist; closing.");
		session.close(true);
	}

	private boolean isBlocked(IoSession session) {
		SocketAddress remoteAddress = session.getRemoteAddress();
		if (remoteAddress instanceof InetSocketAddress) {
			InetAddress address = ((InetSocketAddress) remoteAddress).getAddress();

			// check all subnets
			for (Subnet subnet : whitelist) {
				if (subnet.inSubnet(address)) {
					return false;
				}
			}
		}
		return true;
	}

}
