//package com.kuailu.im.server.helper.redis;
//
//import com.kuailu.im.core.cache.redis.JedisTemplate;
//import com.kuailu.im.core.cache.redis.RedisCacheManager;
//import com.kuailu.im.core.packets.ChatMsgReadConfirmBody;
//import com.kuailu.im.server.config.ImServerConfig;
//import com.kuailu.im.server.config.PropertyImServerConfigBuilder;
//import org.junit.jupiter.api.Test;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static com.kuailu.im.core.ImConst.*;
//import static org.junit.jupiter.api.Assertions.*;
//
//class RedisMessageHelperTest {
//
//    RedisMessageHelper redisMessageHelper = new RedisMessageHelper();
//
//    @Test
//    void getBindListener() {
//    }
//
//    @Test
//    void isOnline() {
//    }
//
//    @Test
//    void putChatGroupMsgConfirm() throws Exception {
//        ImServerConfig imServerConfig = new PropertyImServerConfigBuilder("config/jim.properties").build();
//        RedisCacheManager.register(USER, Integer.MAX_VALUE, Integer.MAX_VALUE);
//        RedisCacheManager.register(GROUP, Integer.MAX_VALUE, Integer.MAX_VALUE);
//        RedisCacheManager.register(STORE, Integer.MAX_VALUE, Integer.MAX_VALUE);
//        RedisCacheManager.register(PUSH, Integer.MAX_VALUE, Integer.MAX_VALUE);
//        RedisCacheManager.register(TERMINAL, Integer.MAX_VALUE, Integer.MAX_VALUE);
//        ChatMsgReadConfirmBody chatMsgReadConfirmBody = new ChatMsgReadConfirmBody();
//        chatMsgReadConfirmBody.setGroupId("cha");
//        redisMessageHelper.putChatGroupMsgConfirm("1111", "222", chatMsgReadConfirmBody);
//        redisMessageHelper.putChatGroupMsgConfirm("13", "221232", chatMsgReadConfirmBody);
//        redisMessageHelper.putChatGroupMsgConfirm("sada", "2asda21232", chatMsgReadConfirmBody);
//
//        List<ChatMsgReadConfirmBody> chatMsgReadConfirmBodies = new ArrayList<>();
//        for (int i = 0; i < 5; i++) {
//            ChatMsgReadConfirmBody chatMsgReadConfirmBody2 = new ChatMsgReadConfirmBody();
//            chatMsgReadConfirmBody.setGroupId("cha" + i);
//            chatMsgReadConfirmBodies.add(chatMsgReadConfirmBody2);
//        }
//
//        System.out.println(redisMessageHelper.getChatGroupMsgConfirm("111", "222"));
//
//    }
//
//    @Test
//    void getChatGroupMsgConfirm() throws Exception {
//        ImServerConfig imServerConfig = new PropertyImServerConfigBuilder("config/jim.properties").build();
//        RedisCacheManager.register(TERMINAL, Integer.MAX_VALUE, Integer.MAX_VALUE);
//        RedisCacheManager.getCache(TERMINAL).put("as1131d", "sdad");
//        JedisTemplate.me().set("terminal:asd1", 123);
//        JedisTemplate.me().set("terminal:asd2", 234);
//        JedisTemplate.me().set("terminal:asd3", "sdfsd");
//        JedisTemplate.me().set("terminal:asd4", 124243);
//        JedisTemplate.me().set("terminal:asd5", 122342433);
//    }
//}