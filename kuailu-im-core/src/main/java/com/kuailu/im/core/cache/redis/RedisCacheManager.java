//package com.kuailu.im.core.cache.redis;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * @author linjd
// * @date 2022年8月8日 下午2:35:19
// */
//public class RedisCacheManager {
//
//	private static Logger log = LoggerFactory.getLogger(RedisCache.class);
//	private static Map<String, RedisCache> map = new HashMap<>();
//
//	public static RedisCache getCache(String cacheName) {
//		RedisCache redisCache = map.get(cacheName);
//		if (redisCache == null) {
//			log.error("cacheName[{}]还没注册，请初始化时调用：{}.register(redisson, cacheName, timeToLiveSeconds, timeToIdleSeconds)", cacheName, RedisCache.class.getSimpleName());
//		}
//		return redisCache;
//	}
//
//	/**
//	 * timeToLiveSeconds和timeToIdleSeconds不允许同时为null
//	 * @param cacheName
//	 * @param timeToLiveSeconds
//	 * @param timeToIdleSeconds
//	 * @return
//	 * @author linjd
//	 */
//	public static RedisCache register(String cacheName, Integer timeToLiveSeconds, Integer timeToIdleSeconds) {
//		RedisExpireUpdateTask.start();
//
//		RedisCache redisCache = map.get(cacheName);
//		if (redisCache == null) {
//			synchronized (RedisCacheManager.class) {
//				redisCache = map.get(cacheName);
//				if (redisCache == null) {
//					redisCache = new RedisCache(cacheName, timeToLiveSeconds, timeToIdleSeconds);
//					map.put(cacheName, redisCache);
//				}
//			}
//		}
//		return redisCache;
//	}
//
//
//}
