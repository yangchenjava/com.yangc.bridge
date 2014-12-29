package com.yangc.bridge.comm.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;

public class NativeCache implements Cache {

	private ConcurrentMap<String, Long> cache = new ConcurrentHashMap<String, Long>();

	@Override
	public Long get(String key) {
		if (StringUtils.isNotBlank(key)) {
			return this.cache.get(key);
		}
		return null;
	}

	@Override
	public Long put(String key, Long value) {
		if (StringUtils.isNotBlank(key) && value != null) {
			return this.cache.put(key, value);
		}
		return null;
	}

	@Override
	public Long remove(String key) {
		if (StringUtils.isNotBlank(key)) {
			return this.cache.remove(key);
		}
		return null;
	}

	@Override
	public void clear() {
		this.cache.clear();
	}

	@Override
	public boolean containsKey(String key) {
		return this.cache.containsKey(key);
	}

	@Override
	public long size() {
		return this.cache.size();
	}

	@Override
	public Map<String, Long> map() {
		return new HashMap<String, Long>(this.cache);
	}

}
