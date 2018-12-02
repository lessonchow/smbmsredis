package cn.smbms.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.smbms.dao.RedisDao;
import cn.smbms.tools.Constants;

@Service
public class RedisServiceImpl implements RedisService{

	@Autowired
	private RedisDao redisDaoImpl;
	
	/**
	 * method of adding the key and value into Redis  
	 * @param key
	 * @param value
	 * @param timeout the unit is {@link TimeUnit.SECONDS}
	 */
	@Override
	public void set(String key, String value, long timeout) throws Exception{
		redisDaoImpl.set(key, value.getBytes("utf-8"), timeout);
	}
	
	/**
	 * method of the getting the value of the given key
	 * @param key
	 * @return the value based on utf-8
	 */
	@Override
	public String get(String key) throws Exception{
		byte[] byteValues = redisDaoImpl.get(key);
		if (byteValues == null) {
			return null;
		} else {
			return new String(byteValues, "utf-8");
		}
	}
	
	/**
	 * method of changing the expired time of the key
	 * @param key
	 * @param timeout the unit is {@link TimeUnit.SECONDS}
	 */
	@Override
	public void expire(String key, long timeout) throws Exception{
		redisDaoImpl.expire(key, timeout);
	}
	
	/**
	 * method of setting new fields and values to the into Redis
	 * @param <T>
	 * @param key
	 * @param field
	 * @param m
	 * @throws Exception 
	 */
	@Override
	public <T> boolean hsetInPage(String key, List<T> list) throws Exception {
		Map<String, Object> m = new HashMap<String, Object>();
		List<T> temp = new ArrayList<T>();
		int pageNum = 0;
		try { //因为有两次访问Redis，所以要try catch块来达到一定的事务功能
			if (list != null && list.size() != 0) {
				redisDaoImpl.hset(key, "count", list.size());
				for (int i = 0; i < list.size(); i++) {
					temp.add(list.get(i));
					pageNum = (int) Math.ceil((double) (i + 1) / Constants.pageSize);
					if (i != 0 && i < list.size() - 1 && (i + 1) % Constants.pageSize == 0) {
						m.put(String.valueOf(pageNum), temp);
						temp = new ArrayList<T>();
					} else if (i == list.size() - 1) {
						m.put(String.valueOf(pageNum), temp);
					}
				}
				redisDaoImpl.hset(key, m);
				return true;
			}
		} catch (Exception e) {
			throw e;
		}
		return false;
	}
	
	/**
	 * method of getting the value of specific key under specific key
	 * @param key
	 * @param field
	 * @throws Exception 
	 */
	@Override
	public Object hget(String key,String field) throws Exception {
		return redisDaoImpl.hget(key, field);
	}
	
	/**
	 * method of setting new field and value into Redis
	 * @param key
	 * @param field
	 * @param value
	 */
	@Override
	public void hset(String key, String field, Object value) {
		redisDaoImpl.hset(key, field, value);
	}
	
	/**
	 * method of checking if the key exists
	 * @param key
	 */
	@Override
	public boolean hasKey(String key) {
		return redisDaoImpl.hasKey(key);
	}
}
