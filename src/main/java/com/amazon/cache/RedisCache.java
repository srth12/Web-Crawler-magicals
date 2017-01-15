package com.amazon.cache;

import org.apache.log4j.Logger;

import com.amazon.dao.FileHandler;

import redis.clients.jedis.Jedis;

/**
 * {@link https://www.tutorialspoint.com/redis/redis_java.htm}
 * @author Sarath Babu
 *
 */

public class RedisCache {
	
	final static Logger logger = Logger.getLogger(RedisCache.class);
	
	public static void main(String[] args) {
		// Connecting to Redis server on localhost
		Jedis jedis = new Jedis("localhost");
		System.out.println("Connection to server sucessfully");
		logger.info("Connection to server sucessfully");
		// check whether server is running or not
//		jedis.set("tutorial-name", "Redis tutorial");
		System.out.println("Server is running: " + jedis.ping());
	}

}
