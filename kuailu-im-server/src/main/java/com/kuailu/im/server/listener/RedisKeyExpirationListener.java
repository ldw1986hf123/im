//package com.kuailu.im.server.listener;
//
//import com.google.gson.JsonObject;
//import com.kuailu.im.server.constant.RedisCacheKey;
//import com.kuailu.im.server.service.IatStarter;
//import com.kuailu.im.server.util.GuavaCache;
//import com.kuailu.im.server.util.RedisService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.connection.Message;
//import org.springframework.data.redis.connection.MessageListener;
//import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
//import org.springframework.data.redis.listener.PatternTopic;
//import org.springframework.data.redis.listener.RedisMessageListenerContainer;
//import org.springframework.stereotype.Component;
//
//@Component
//@Slf4j
//public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {
//    @Autowired
//    GuavaCache guavaCache;
//
//    @Autowired
//    RedisService redisService;
//
//
//    /**
//     * 该方法是多线程的，redis中每放进去一个key，如果往redis里面放值太快，  if (!userIdToIatStarter.containsKey(key))就会出现线程安全问题
//     * todo 用threadLocal怎么解决？？？
//     * todo ConcurrentHashMap 怎么解决
//     *
//     * @param message
//     * @param pattern
//     */
//    @Override
//    public void onMessage(Message message, byte[] pattern) {
//        // 过期key
//        String expiredKey = message.toString();
//        // 上下文获取租户对象，此时租户对象为初始化对象，属性均为null；
//        String key = new String(message.getBody());
//        if (key.startsWith(RedisCacheKey.USER_BYTE_KEY)) {
//            log.info("key :{} 过期", key);
//            String userId = key.substring( RedisCacheKey.USER_BYTE_KEY.length() );
//            IatStarter iatStarter = guavaCache.get(userId, IatStarter.class);
//            if (null != iatStarter) {
//                iatStarter.sendEnd("end");
//                guavaCache.invalidate(key);
//            }
//        }
//    }
//
//    public RedisKeyExpirationListener(RedisMessageListenerContainer redisMessageListenerContainer) {
//        super(redisMessageListenerContainer);
//    }
//}