package com.yangc.bridge.comm.cache;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.yangc.utils.Message;

@Service
public class SessionCache {

	private static final Logger logger = Logger.getLogger(SessionCache.class);

	private Cache cache;

	public SessionCache() {
		if (StringUtils.equals(Message.getMessage("bridge.cache"), "native")) {
			logger.info("==========native缓存连接信息=========");
			this.cache = new NativeCache();
		} else {
			logger.info("==========redis缓存连接信息=========");
			this.cache = new RedisCache();
		}
	}

	public Long getSessionId(String username) {
		return this.cache.get(username);
	}

	public void putSessionId(String username, Long sessionId) {
		this.cache.put(username, sessionId);
	}

	public Long removeSessionId(String username) {
		return this.cache.remove(username);
	}

	public void clear() {
		this.cache.clear();
	}

	public boolean contains(String username) {
		return this.cache.containsKey(username);
	}

	public Map<String, Long> getSessionCache() {
		return this.cache.map();
	}

}
