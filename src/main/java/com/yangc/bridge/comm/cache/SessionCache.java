package com.yangc.bridge.comm.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.mina.core.session.IoSession;

public class SessionCache {

	private static final Map<String, IoSession> map = new HashMap<String, IoSession>();

	public static IoSession getSession(String jid) {
		return map.get(jid);
	}

	public static void putSession(String jid, IoSession session) {
		map.put(jid, session);
	}

	public static void removeSession(String ipAddress) {
		Set<String> jids = map.keySet();
		for (String jid : jids) {
			if (jid.contains(ipAddress)) {
				map.remove(jid);
				break;
			}
		}
	}

}
