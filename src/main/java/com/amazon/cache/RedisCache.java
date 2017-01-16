package com.amazon.cache;

import java.util.List;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;

/**
 * {@link https://www.tutorialspoint.com/redis/redis_java.htm}
 * @author Sarath Babu
 *
 */

public class RedisCache {
	
	final static Logger logger = Logger.getLogger(RedisCache.class);
	private Jedis jedis;
	public RedisCache(){
		jedis = new Jedis("localhost");
	}
	public static void main(String[] args) {
		// Connecting to Redis server on localhost
		Jedis jedis = new Jedis("localhost");
		System.out.println("Connection to server sucessfully");
		logger.info("Connection to server sucessfully");
		// check whether server is running or not
//		jedis.set("tutorial-name", "Redis tutorial");
		System.out.println("Server is running: " + jedis.ping());
	}

	public void insertDataToList(String listName, String value){
		jedis.lpush(listName, value);
	}
	
	public List<String> getAllDataFromList(String listName){
		List<String> result = jedis.lrange(listName, 0, -1);
		jedis.ltrim(listName, 0, -1);
		return result;
	}
}
