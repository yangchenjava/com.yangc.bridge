package com.yangc.bridge.comm;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.log4j.Logger;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yangc.bridge.comm.codec.DataCodecFactory;
import com.yangc.bridge.comm.handler.ServerHandler;
import com.yangc.system.service.UserService;
import com.yangc.utils.Message;

@Service("com.yangc.bridge.comm.Server")
public class Server {

	private static final Logger logger = Logger.getLogger(Server.class);

	private static final String IP = Message.getMessage("bridge.ipAddress");
	private static final int PORT = Integer.parseInt(Message.getMessage("bridge.port"));
	private static final int TIMEOUT = Integer.parseInt(Message.getMessage("bridge.timeout"));

	@Autowired
	private UserService userService;

	private IoAcceptor acceptor;

	private void init() {
		this.acceptor = new NioSocketAcceptor();
		// 设置空闲时间
		this.acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, TIMEOUT);
		// 设置过滤器
		DefaultIoFilterChainBuilder filterChain = this.acceptor.getFilterChain();
		filterChain.addLast("codec", new ProtocolCodecFilter(new DataCodecFactory()));
		this.acceptor.setHandler(new ServerHandler());
		try {
			this.acceptor.bind(new InetSocketAddress(IP, PORT));
		} catch (IOException e) {
			e.printStackTrace();
		}
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

}
