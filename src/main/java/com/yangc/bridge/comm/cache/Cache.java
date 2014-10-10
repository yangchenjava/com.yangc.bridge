package com.yangc.bridge.comm.cache;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Cache<K, V> {

	public V get(K key);

	public V put(K key, V value);

	public V remove(K key);

	public void clear();

	public boolean containsKey(K key);

	public int size();

	public Set<K> keys();

	public List<V> values();

	public Map<K, V> map();

}
