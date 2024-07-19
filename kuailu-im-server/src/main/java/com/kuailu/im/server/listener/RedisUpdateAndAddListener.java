//package com.kuailu.im.server.listener;
//
//import com.google.gson.JsonObject;
//import com.kuailu.im.server.constant.RedisCacheKey;
//import com.kuailu.im.server.service.IatStarter;
//import com.kuailu.im.server.util.GuavaCache;
//import com.kuailu.im.server.util.RedisService;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.connection.Message;
//import org.springframework.data.redis.connection.MessageListener;
//import org.springframework.data.redis.listener.PatternTopic;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//import static com.kuailu.im.server.constant.RedisCacheKey.USER_BYTE_KEY;
//
//@Component
//@Slf4j
//public class RedisUpdateAndAddListener implements MessageListener {
//    //监听的主题
//    private final PatternTopic topicSet = new PatternTopic("__keyevent@*__:set");
//    private final PatternTopic topicExpire = new PatternTopic("__keyevent@*__:expired");
//    @Autowired
//    RedisService redisService;
//    @Autowired
//    GuavaCache guavaCache;
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
//        // 上下文获取租户对象，此时租户对象为初始化对象，属性均为null；
//        String key = new String(message.getBody());
//        String topic = new String(pattern);
//        if (topicExpire.toString().equals(topic)) {
//            log.info("userId :{} 过期", key);
//            JsonObject frame2 = new JsonObject();
//            JsonObject data2 = new JsonObject();
//            data2.addProperty("status", 2);
//            data2.addProperty("audio", "");
//            data2.addProperty("format", "audio/L16;rate=16000");
//            data2.addProperty("encoding", "raw");
//            frame2.add("data", data2);
//            log.info("all data is send");
//            guavaCache.get(key, IatStarter.class).send("end", "");
//            guavaCache.invalidate(key);
//        }
//    }
//
//    public List<PatternTopic> getTopicList() {
//        List<PatternTopic> topicList = new ArrayList<>();
//        topicList.add(topicExpire);
//        topicList.add(topicSet);
//        return topicList;
//    }
//}