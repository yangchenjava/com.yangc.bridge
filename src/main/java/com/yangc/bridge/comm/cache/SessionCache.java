package com.yangc.bridge.comm.cache;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class SessionCache {

	private static final Map<String, Long> map = new HashMap<String, Long>();

	public static Long getSessionId(String username) {
		return map.get(username);
	}

	public static void putSessionId(String username, Long id) {
		map.put(username, id);
	}

	public static void removeSessionId(Long id) {
		String key = null;
		for (String username : map.keySet()) {
			if (map.get(username).longValue() == id.longValue()) {
				key = username;
				break;
			}
		}
		if (StringUtils.isNotBlank(key)) {
			map.remove(key);
		}
	}

}
