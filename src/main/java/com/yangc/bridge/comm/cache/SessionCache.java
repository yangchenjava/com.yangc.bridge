package com.yangc.bridge.comm.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SessionCache {

	private static final ConcurrentMap<String, Long> map = new ConcurrentHashMap<String, Long>();

	public static Long getSessionId(String username) {
		return map.get(username);
	}

	public static void putSessionId(String username, Long id) {
		map.put(username, id);
	}

	public static void removeSessionId(Long id) {
		for (String username : map.keySet()) {
			if (map.get(username).longValue() == id.longValue()) {
				map.remove(username);
				break;
			}
		}
	}

}
