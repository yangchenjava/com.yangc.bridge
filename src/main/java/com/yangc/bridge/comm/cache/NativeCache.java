package com.yangc.bridge.comm.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class NativeCache<K, V> implements Cache<K, V> {

	private ConcurrentMap<K, V> cache = new ConcurrentHashMap<K, V>();

	@Override
	public V get(K key) {
		if (key != null) {
			return this.cache.get(key);
		}
		return null;
	}

	@Override
	public V put(K key, V value) {
		if (key != null && value != null) {
			return this.cache.put(key, value);
		}
		return null;
	}

	@Override
	public V remove(K key) {
		if (key != null) {
			return this.cache.remove(key);
		}
		return null;
	}

	@Override
	public void clear() {
		this.cache.clear();
	}

	@Override
	public boolean containsKey(K key) {
		return this.cache.containsKey(key);
	}

	@Override
	public int size() {
		return this.cache.size();
	}

	@Override
	public Set<K> keys() {
		return this.cache.keySet();
	}

	@Override
	public List<V> values() {
		List<V> values = new ArrayList<V>();
		for (V value : this.cache.values()) {
			values.add(value);
		}
		return values;
	}

	@Override
	public Map<K, V> map() {
		return new HashMap<K, V>(this.cache);
	}

}
