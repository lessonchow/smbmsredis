package cn.smbms.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.smbms.tools.Constants;

public interface RedisService {

	/**
	 * method of adding the key and value into Redis  
	 * @param key
	 * @param value
	 * @param timeout the unit is {@link TimeUnit.SECONDS}
	 */
	public void set(String key, String value, long timeout) throws Exception;
	
	/**
	 * method of the getting the value of the given key
	 * @param key
	 * @return the value based on utf-8
	 */
	public String get(String key) throws Exception;
	
	/**
	 * method of changing the expired time of the key
	 * @param key
	 * @param timeout the unit is {@link TimeUnit.SECONDS}
	 */
	public void expire(String key, long timeout) throws Exception;
	
	/**
	 * method of setting new fields and values to the into Redis
	 * @param <T>
	 * @param key
	 * @param field
	 * @param m
	 * @return 
	 * @throws Exception 
	 */
	public <T> boolean hsetInPage(String key, List<T> list) throws Exception;
	
	/**
	 * method of getting the value of specific key under specific key
	 * @param key
	 * @param field
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
