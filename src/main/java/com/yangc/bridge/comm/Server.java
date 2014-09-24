package com.yangc.bridge.comm;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.firewall.BlacklistFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yangc.bridge.bean.ClientStatus;
import com.yangc.bridge.bean.ServerStatus;
import com.yangc.bridge.comm.cache.SessionCache;
import com.yangc.bridge.comm.factory.DataCodecFactory;
import com.yangc.bridge.comm.handler.ServerHandler;
import com.yangc.utils.Message;

@Service("com.yangc.bridge.comm.Server")
public class Server {

	private static final Logger logger = Logger.getLogger(Server.class);

	private static final String IP = Message.getMessage("bridge.ipAddress");
	private static final int PORT = Integer.parseInt(Message.getMessage("bridge.port"));
	private static final int TIMEOUT = Integer.parseInt(Message.getMessage("bridge.timeout"));
	public static final String CODEC = Message.getMessage("bridge.codec");

	public static final String CHARSET_NAME = "UTF-8";

	@Autowired
	private ServerHandler serverHandler;

	private NioSocketAcceptor acceptor;

	private void init() {
		this.acceptor = new NioSocketAcceptor();
		// 设置的是主服务监听的端口可以重用
		this.acceptor.setReuseAddress(true);
		// 客户端最大连接数
		// this.acceptor.setBacklog(1000);
		// 设置空闲时间
		this.acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, TIMEOUT);
		// 设置每一个非主监听连接的端口可以重用
		this.acceptor.getSessionConfig().setReuseAddress(true);
		// 设置过滤器
		DefaultIoFilterChainBuilder filterChain = this.acceptor.getFilterChain();
		// 编解码
		filterChain.addLast("codec", new ProtocolCodecFilter(new DataCodecFactory()));
		// 黑名单
		BlacklistFilter blacklistFilter = this.getBlacklistFilter();
		if (blacklistFilter != null) {
			filterChain.addLast("blacklist", blacklistFilter);
		}
		// 线程池
		// filterChain.addLast("threadPool", new ExecutorFilter(Executors.newCachedThreadPool()));
		this.acceptor.setHandler(this.serverHandler);
		try {
			this.acceptor.bind(new InetSocketAddress(IP, PORT));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private BlacklistFilter getBlacklistFilter() {
		String blacklist = Message.getMessage("bridge.blacklist");
		if (StringUtils.isNotBlank(blacklist)) {
			String[] hosts = blacklist.split(",");
			Set<InetAddress> addresses = new HashSet<InetAddress>(hosts.length);
			for (String host : hosts) {
				try {
					addresses.add(InetAddress.getByName(host));
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			}
			BlacklistFilter blacklistFilter = new BlacklistFilter();
			blacklistFilter.setBlacklist(addresses);
			return blacklistFilter;
		}
		return null;
	}

	public void start() {
		logger.info("==========mina服务启动=========");
		this.init();
	}

	public void restart() {
		logger.info("==========重启mina服务=========");
		if (this.acceptor != null) {
			this.acceptor.dispose();
			this.acceptor = null;
		}
		this.init();
	}

	public boolean isActive() {
		if (this.acceptor != null) {
			return this.acceptor.isActive();
		}
		return false;
	}

	public ServerStatus getServerStatus() {
		ServerStatus serverStatus = new ServerStatus();
		serverStatus.setIpAddress(IP);
		serverStatus.setPort(PORT);
		serverStatus.setTimeout(TIMEOUT);
		serverStatus.setActive(this.isActive());
		return serverStatus;
	}

	public List<ClientStatus> getClientStatusList() {
		Map<String, Long> map = SessionCache.getSessionCache();
		Map<Long, IoSession> managedSessions = this.acceptor.getManagedSessions();

		List<ClientStatus> clientStatusList = new ArrayList<ClientStatus>(map.size());
		for (Entry<String, Long> entry : map.entrySet()) {
			IoSession session = managedSessions.get(entry.getValue());
			if (session != null) {
				ClientStatus clientStatus = new ClientStatus();
				clientStatus.setUsername(entry.getKey());
				clientStatus.setIpAddress(((InetSocketAddress) session.getRemoteAddress()).getAddress().getHostAddress());
				clientStatus.setSessionId(entry.getValue());
				clientStatus.setLastIoTime(DateFormatUtils.format(session.getLastIoTime(), "yyyy-MM-dd HH:mm:ss"));
				clientStatusList.add(clientStatus);
			}
		}
		return clientStatusList;
	}

	public boolean isOnline(String username) {
		return SessionCache.getSessionId(username) != null;
	}

}
