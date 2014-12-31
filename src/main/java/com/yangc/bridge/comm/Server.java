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

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.session.IdleStatus;
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
	@Autowired
	private SessionCache sessionCache;

	private NioSocketAcceptor acceptor;

	private void init() {
		this.acceptor = new NioSocketAcceptor();
		// 设置主服务监听的端口可以重用
		this.acceptor.setReuseAddress(true);
		// 最大客户端等待队列
		// this.acceptor.setBacklog(50);
		// 设置空闲时间
		this.acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, TIMEOUT);
		// 设置每一个非主服务监听的端口可以重用
		this.acceptor.getSessionConfig().setReuseAddress(true);
		// 设置过滤器
		DefaultIoFilterChainBuilder filterChain = this.acceptor.getFilterChain();
		// 黑名单
		BlacklistFilter blacklistFilter = this.getBlacklistFilter();
		if (blacklistFilter != null) {
			filterChain.addLast("blacklist", blacklistFilter);
		}
		// 编解码
		filterChain.addLast("codec", new ProtocolCodecFilter(new DataCodecFactory()));
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

	/**
	 * @功能: mina服务启动
	 * @作者: yangc
	 * @创建日期: 2014年12月30日 下午2:06:18
	 */
	public void start() {
		logger.info("==========mina服务启动=========");
		this.init();
	}

	/**
	 * @功能: 重启mina服务
	 * @作者: yangc
	 * @创建日期: 2014年12月30日 下午2:06:27
	 */
	public void restart() {
		logger.info("==========重启mina服务=========");
		if (this.acceptor != null) {
			this.acceptor.dispose();
			this.acceptor = null;
		}
		this.sessionCache.clear();
		this.init();
	}

	/**
	 * @功能: mina服务是否存活
	 * @作者: yangc
	 * @创建日期: 2014年12月30日 下午2:06:35
	 * @return
	 */
	public boolean isActive() {
		if (this.acceptor != null) {
			return this.acceptor.isActive();
		}
		return false;
	}

	/**
	 * @功能: 获取mina服务端状态
	 * @作者: yangc
	 * @创建日期: 2014年12月30日 下午2:06:56
	 * @return
	 */
	public ServerStatus getServerStatus() {
		ServerStatus serverStatus = new ServerStatus();
		serverStatus.setIpAddress(IP);
		serverStatus.setPort(PORT);
		serverStatus.setTimeout(TIMEOUT);
		serverStatus.setActive(this.isActive());
		return serverStatus;
	}

	/**
	 * @功能: 分页获取客户端连接的状态
	 * @作者: yangc
	 * @创建日期: 2014年12月30日 下午2:07:16
	 * @return
	 */
	public List<ClientStatus> getClientStatusList_page() {
		List<ClientStatus> clientStatusList = new ArrayList<ClientStatus>();
		Map<String, Long> map = this.sessionCache.getSessionCache();
		if (MapUtils.isNotEmpty(map)) {
			for (Entry<String, Long> entry : map.entrySet()) {
				ClientStatus clientStatus = new ClientStatus();
				clientStatus.setUsername(entry.getKey());
				clientStatus.setSessionId(entry.getValue());
				clientStatusList.add(clientStatus);
			}
		}
		return clientStatusList;
	}

	/**
	 * @功能: 判断客户端是否在线
	 * @作者: yangc
	 * @创建日期: 2014年12月30日 下午8:31:41
	 * @param username
	 * @return
	 */
	public boolean isOnline(String username) {
		return this.sessionCache.getSessionId(username) != null;
	}

}
