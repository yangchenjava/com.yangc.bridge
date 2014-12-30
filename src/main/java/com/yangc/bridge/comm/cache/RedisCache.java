package com.yangc.bridge.comm.cache;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.Tuple;

import com.yangc.common.Pagination;
import com.yangc.common.PaginationThreadUtils;
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

			/* 获取分页情况 */
			Pagination pagination = PaginationThreadUtils.get();
			if (pagination == null) {
				pagination = new Pagination();
				PaginationThreadUtils.set(pagination);
				pagination.setPageNow(1);
			}
			if (pagination.getTotalCount() == 0) {
				pagination.setTotalCount(jedis.zcard(CACHE_NAME).intValue());
			}
			int firstResult = (pagination.getPageNow() - 1) * pagination.getPageSize();
			/* 校验分页情况 */
			if (firstResult >= pagination.getTotalCount() || firstResult < 0) {
				firstResult = 0;
				pagination.setPageNow(1);
			}
			/* 如果总数返回0, 直接返回空 */
			if (pagination.getTotalCount() == 0) {
				return null;
			}
			Set<Tuple> tuples = jedis.zrangeWithScores(CACHE_NAME, firstResult, firstResult + pagination.getPageSize() - 1);

			Map<String, Long> map = new TreeMap<String, Long>();
			for (Tuple tuple : tuples) {
				map.put(tuple.getElement(), (long) tuple.getScore());
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
