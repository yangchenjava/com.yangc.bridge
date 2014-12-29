package com.yangc.bridge.comm.cache;

import java.util.Map;

public interface Cache {

	public Long get(String key);

	public Long put(String key, Long value);

	public Long remove(String key);

	public void clear();

	public boolean containsKey(String key);

	public long size();

	public Map<String, Long> map();

}
