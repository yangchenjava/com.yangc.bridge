package com.yangc.bridge.comm.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.shiro.cache.CacheException;

import redis.clients.jedis.ShardedJedis;

import com.yangc.utils.Message;
import com.yangc.utils.cache.RedisUtils;
import com.yangc.utils.io.SerializeUtils;

public class RedisCache<K, V> implements Cache<K, V> {

	private byte[] cacheName = Message.getMessage("bridge.cache_name").getBytes();

	@Override
	@SuppressWarnings("unchecked")
	public V get(K key) {
		if (key != null) {
			RedisUtils cache = RedisUtils.getInstance();
			ShardedJedis jedis = null;
			try {
				jedis = cache.getJedis();
				return (V) SerializeUtils.deserialize(jedis.hget(this.cacheName, SerializeUtils.serialize(key)));
			} catch (Exception e) {
				e.printStackTrace();
				cache.returnBrokenResource(jedis);
				throw new CacheException();
			} finally {
				cache.returnResource(jedis);
			}
		}
		return null;
	}

	@Override
	public V put(K key, V value) {
		if (key != null && value != null) {
			RedisUtils cache = RedisUtils.getInstance();
			ShardedJedis jedis = null;
			try {
				jedis = cache.getJedis();
				jedis.hset(this.cacheName, SerializeUtils.serialize(key), SerializeUtils.serialize(value));
				return value;
			} catch (Exception e) {
				e.printStackTrace();
				cache.returnBrokenResource(jedis);
				throw new CacheException();
			} finally {
				cache.returnResource(jedis);
			}
		}
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public V remove(K key) {
		if (key != null) {
			RedisUtils cache = RedisUtils.getInstance();
			ShardedJedis jedis = null;
			try {
				jedis = cache.getJedis();
				V value = (V) SerializeUtils.deserialize(jedis.hget(this.cacheName, SerializeUtils.serialize(key)));
				jedis.hdel(this.cacheName, SerializeUtils.serialize(key));
				return value;
			} catch (Exception e) {
				e.printStackTrace();
				cache.returnBrokenResource(jedis);
				throw new CacheException();
			} finally {
				cache.returnResource(jedis);
			}
		}
		return null;
	}

	@Override
	public void clear() {
		RedisUtils cache = RedisUtils.getInstance();
		ShardedJedis jedis = null;
		try {
			jedis = cache.getJedis();
			jedis.del(this.cacheName);
		} catch (Exception e) {
			e.printStackTrace();
			cache.returnBrokenResource(jedis);
			throw new CacheException();
		} finally {
			cache.returnResource(jedis);
		}
	}

	@Override
	public boolean containsKey(K key) {
		RedisUtils cache = RedisUtils.getInstance();
		ShardedJedis jedis = null;
		try {
			jedis = cache.getJedis();
			return jedis.hexists(this.cacheName, SerializeUtils.serialize(key));
		} catch (Exception e) {
			e.printStackTrace();
			cache.returnBrokenResource(jedis);
		} finally {
			cache.returnResource(jedis);
		}
		return false;
	}

	@Override
	public int size() {
		RedisUtils cache = RedisUtils.getInstance();
		ShardedJedis jedis = null;
		try {
			jedis = cache.getJedis();
			return jedis.hlen(this.cacheName).intValue();
		} catch (Exception e) {
			e.printStackTrace();
			cache.returnBrokenResource(jedis);
		} finally {
			cache.returnResource(jedis);
		}
		return 0;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<K> keys() {
		RedisUtils cache = RedisUtils.getInstance();
		ShardedJedis jedis = null;
		try {
			jedis = cache.getJedis();
			Set<K> keys = new HashSet<K>();
			for (byte[] b : jedis.hkeys(this.cacheName)) {
				keys.add((K) SerializeUtils.deserialize(b));
			}
			return keys;
		} catch (Exception e) {
			e.printStackTrace();
			cache.returnBrokenResource(jedis);
		} finally {
			cache.returnResource(jedis);
		}
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<V> values() {
		RedisUtils cache = RedisUtils.getInstance();
		ShardedJedis jedis = null;
		try {
			jedis = cache.getJedis();
			List<V> values = new ArrayList<V>();
			for (byte[] b : jedis.hvals(this.cacheName)) {
				values.add((V) SerializeUtils.deserialize(b));
			}
			return values;
		} catch (Exception e) {
			e.printStackTrace();
			cache.returnBrokenResource(jedis);
		} finally {
			cache.returnResource(jedis);
		}
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<K, V> map() {
		RedisUtils cache = RedisUtils.getInstance();
		ShardedJedis jedis = null;
		try {
			jedis = cache.getJedis();
			Map<K, V> map = new HashMap<K, V>();
			for (Entry<byte[], byte[]> entry : jedis.hgetAll(this.cacheName).entrySet()) {
				map.put((K) SerializeUtils.deserialize(entry.getKey()), (V) SerializeUtils.deserialize(entry.getValue()));
			}
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			cache.returnBrokenResource(jedis);
		} finally {
			cache.returnResource(jedis);
		}
		return null;
	}

}
