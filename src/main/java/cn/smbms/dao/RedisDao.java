package cn.smbms.dao;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface RedisDao {

	/**
	 * method of adding the key and value into Redis  
	 * @param key
	 * @param value the byte[] of the value based on utf-8
	 * @param timeout the unit is {@link TimeUnit.SECONDS}
	 */
	public void set(String key, byte[] value,long timeout) throws Exception;
	
	/**
	 * method of the getting the value of the given key
	 * @param key
	 * @return the byte[] of the value based on utf-8
	 */
	public byte[] get(String key) throws Exception;
	
	/**
	 * method of changing the expired time of the key
	 * @param key
	 * @param timeout the unit is {@link TimeUnit.SECONDS}
	 */
	public void expire(String key, long timeout) throws Exception;
	
	/**
	 * method of setting new fields and values to the into Redis
	 * @param key
	 * @param field
	 * @param m
	 * @throws Exception 
	 */
	public void hset(String key, Map<String, Object> m) throws Exception;
	
	/**
	 * method of getting the value of specific key under specific key
	 * @param key
	 * @param field
	 * @return 
	 * @throws Exception 
	 */
	public Object hget(String key,String field) throws Exception;

	/**
	 * method of setting new field and value into Redis
	 * @param key
	 * @param field
	 * @param value
	 */
	public void hset(String key, String field, Object value);
	
	/**
	 * method of checking if the key exists
	 * @param key
	 * @return 
	 */
	public boolean hasKey(String key);
}
