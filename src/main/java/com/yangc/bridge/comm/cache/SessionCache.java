package com.yangc.bridge.comm.cache;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.yangc.utils.Message;

@Service
public class SessionCache {

	private static final Logger logger = Logger.getLogger(SessionCache.class);

	private Cache<String, Long> cache;

	public SessionCache() {
		if (StringUtils.equals(Message.getMessage("bridge.cache"), "native")) {
			logger.info("==========native缓存连接信息=========");
			this.cache = new NativeCache<String, Long>();
		} else {
			logger.info("==========redis缓存连接信息=========");
			this.cache = new RedisCache<String, Long>();
		}
	}

	public Long getSessionId(String username) {
		return this.cache.get(username);
	}

	public void putSessionId(String username, Long sessionId) {
		this.cache.put(username, sessionId);
	}

	public String removeSessionId(Long sessionId) {
		for (Entry<String, Long> entry : this.cache.map().entrySet()) {
			if (entry.getValue().longValue() == sessionId.longValue()) {
				this.cache.remove(entry.getKey());
				return entry.getKey();
			}
		}
		return null;
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
