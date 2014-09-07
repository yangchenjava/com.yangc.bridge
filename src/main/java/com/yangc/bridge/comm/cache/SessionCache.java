package com.yangc.bridge.comm.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SessionCache {

	private static final ConcurrentMap<String, Long> SESSION_CACHE = new ConcurrentHashMap<String, Long>();

	public static Long getSessionId(String username) {
		return SESSION_CACHE.get(username);
	}

	public static void putSessionId(String username, Long sessionId) {
		SESSION_CACHE.put(username, sessionId);
	}

	public static String removeSessionId(Long sessionId) {
		for (Entry<String, Long> entry : SESSION_CACHE.entrySet()) {
			if (entry.getValue().longValue() == sessionId.longValue()) {
				SESSION_CACHE.remove(entry.getKey());
				return entry.getKey();
			}
		}
		return null;
	}

	public static boolean contains(String username) {
		return SESSION_CACHE.containsKey(username);
	}

	public static Map<String, Long> getSessionCache() {
		return new HashMap<String, Long>(SESSION_CACHE);
	}

}
