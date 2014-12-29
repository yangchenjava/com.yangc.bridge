package com.yangc.bridge.comm.cache;

import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import redis.clients.jedis.ShardedJedis;

import com.yangc.utils.Message;
import com.yangc.utils.cache.RedisUtils;

public class RedisCache implements Cache {

	private static final String CACHE_NAME = Message.getMessage("bridge.cache_name");

	@Override
	public Long get(String key) {
		if (StringUtils.isNotBlank(key)) {
			RedisUtils cache = RedisUtils.getInstance();
			ShardedJedis jedis = null;
			try {
				jedis = cache.getJedis();
				return jedis.zscore(CACHE_NAME, key).longValue();
			} catch (Exception e) {
				e.printStackTrace();
				cache.returnBrokenResource(jedis);
			} finally {
				cache.returnResource(jedis);
			}
		}
		return null;
	}

	@Override
	public Long put(String key, Long value) {
		if (StringUtils.isNotBlank(key) && value != null) {
			RedisUtils cache = RedisUtils.getInstance();
			ShardedJedis jedis = null;
			try {
				jedis = cache.getJedis();
				jedis.zadd(CACHE_NAME, value.doubleValue(), key);
				return value;
			} catch (Exception e) {
				e.printStackTrace();
				cache.returnBrokenResource(jedis);
			} finally {
				cache.returnResource(jedis);
			}
		}
		return null;
	}

	@Override
	public Long remove(String key) {
		if (StringUtils.isNotBlank(key)) {
			RedisUtils cache = RedisUtils.getInstance();
			ShardedJedis jedis = null;
			try {
				jedis = cache.getJedis();
				Long value = jedis.zscore(CACHE_NAME, key).longValue();
				jedis.zrem(CACHE_NAME, key);
				return value;
			} catch (Exception e) {
				e.printStackTrace();
				cache.returnBrokenResource(jedis);
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
			jedis.del(CACHE_NAME);
		} catch (Exception e) {
			e.printStackTrace();
			cache.returnBrokenResource(jedis);
		} finally {
			cache.returnResource(jedis);
		}
	}

	@Override
	public boolean containsKey(String key) {
		RedisUtils cache = RedisUtils.getInstance();
		ShardedJedis jedis = null;
		try {
			jedis = cache.getJedis();
			jedis.zscore(CACHE_NAME, key);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			cache.returnBrokenResource(jedis);
		} finally {
			cache.returnResource(jedis);
		}
		return false;
	}

	@Override
	public long size() {
		RedisUtils cache = RedisUtils.getInstance();
		ShardedJedis jedis = null;
		try {
			jedis = cache.getJedis();
			return jedis.zcard(CACHE_NAME);
		} catch (Exception e) {
			e.printStackTrace();
			cache.returnBrokenResource(jedis);
		} finally {
			cache.returnResource(jedis);
		}
		return 0;
	}

	@Override
	public Map<String, Long> map() {
		RedisUtils cache = RedisUtils.getInstance();
		ShardedJedis jedis = null;
		try {
			jedis = cache.getJedis();
			Map<String, Long> map = new TreeMap<String, Long>();
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
