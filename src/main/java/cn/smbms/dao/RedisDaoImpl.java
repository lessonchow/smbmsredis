package cn.smbms.dao;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Repository;

@Repository
public class RedisDaoImpl implements RedisDao{

	@Autowired
	private RedisTemplate<Object, Object> redisTemplate;
	
	/**
	 * method of adding the key and value into Redis  
	 * @param key
	 * @param value the byte[] of the value based on utf-8
	 * @param timeout the unit is {@link TimeUnit.SECONDS}
	 */
	@Override
	public void set(String key, byte[] value,long timeout) throws Exception{
		redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
	}
	
	/**
	 * method of the getting the value of the given key
	 * @param key
	 * @return the byte[] of the value based on utf-8
	 */
	@Override
	public byte[] get(String key) throws Exception{
		return (byte[]) redisTemplate.opsForValue().get(key);
	}
	
	/**
	 * method of changing the expired time of the key
	 * @param key
	 * @param timeout the unit is {@link TimeUnit.SECONDS}
	 */
	@Override
	public void expire(String key, long timeout) throws Exception{
		redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
	}
	
	/**
	 * method of setting new fields and values to the into Redis
	 * @param key
	 * @param field
	 * @param m
	 */
	@Override
	public void hset(String key, Map<String, Object> m) throws Exception{
		redisTemplate.opsForHash().putAll(key, m);
	}
	
	/**
	 * method of getting the value of specific key under specific key
	 * @param key
	 * @param field
	 */
	@Override
	public Object hget(String key,String field) throws Exception{
		return redisTemplate.opsForHash().get(key, field);
	}
	
	/**
	 * method of setting new field and value into Redis
	 * @param key
	 * @param field
	 * @param value
	 */
	@Override
	public void hset(String key, String field, Object value) {
		redisTemplate.opsForHash().put(key, field, value);
	}
	
	/**
	 * method of checking if the key exists
	 * @param key
	 */
	@Override
	public boolean hasKey(String key) {
		return redisTemplate.hasKey(key);
	}
	
}
