package com.yangc.bridge.comm.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;

public class NativeCache {

	private ConcurrentMap<String, Long> cache = new ConcurrentHashMap<String, Long>();

	public Long get(String key) {
		if (StringUtils.isNotBlank(key)) {
			return this.cache.get(key);
		}
		return null;
	}

	public Long put(String key, Long value) {
		if (StringUtils.isNotBlank(key) && value != null) {
			return this.cache.put(key, value);
		}
		return null;
	}

	public Long remove(String key) {
		if (StringUtils.isNotBlank(key)) {
			return this.cache.remove(key);
		}
		return null;
	}

	public void clear() {
		this.cache.clear();
	}

	public boolean containsKey(String key) {
		return this.cache.containsKey(key);
	}

	public long size() {
		return this.cache.size();
	}

	public Map<String, Long> map() {
		return new HashMap<String, Long>(this.cache);
	}

}
