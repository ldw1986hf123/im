package com.kuailu.im.server.util;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kuailu.im.core.ImPacket;
import com.kuailu.im.core.ImStatus;
import com.kuailu.im.core.packets.Command;
import com.kuailu.im.core.packets.RespBody;
import com.kuailu.im.server.constant.RedisCacheKey;
import com.kuailu.im.server.dto.GroupCacheDto;
import com.kuailu.im.server.model.entity.ChatMsg;
import com.kuailu.im.server.mq.PushMessage;
import com.kuailu.im.server.schduler.MsgHelperScheduledTask;
import com.kuailu.im.server.service.IChatMsgService;
import com.kuailu.im.server.starter.BaseJunitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


class RedisServiceTest extends BaseJunitTest {

    @Autowired
    RedisService redisService;


    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    IChatMsgService chatMsgService;

    @Autowired
    MsgHelperScheduledTask msgHelperScheduledTask;

    @Test
    void expire() {
        String key = RedisCacheKey.ONLINE_USER_CACHE + "3856eca9-c16c-4c75-b0f3-8bdb34afdea7";
        List<GroupCacheDto> groupCacheDtoList = redisService.getHashValue(key, "groups", ArrayList.class);
        printResult(groupCacheDtoList);

        GroupCacheDto groupCacheDto1 = groupCacheDtoList.get(0);
        groupCacheDto1.setPublicChatUnReadCount(11);

    }

    @Test
    void testExpire() {
    }

    @Test
    void hasKey() {
        System.out.println(redisService.hasKey("SINGLE_USER_BYTES_701b1947-693a-4d6a-b646-0901e9d94b47"));

    }

    @Test
    void delKey() {
    }

    @Test
    void delKeys() {
        RespBody resPacket = new RespBody(Command.COMMAND_WS_CONNECTED, ImStatus.OK);
        ImPacket imPacket = new ImPacket();
        imPacket.setBody(resPacket.toByte());
        imPacket.setCommand(Command.COMMAND_WS_CONNECTED);
        printResult(imPacket);
    }


    @Test
    void setList() {
        List<String> list1 = new ArrayList<>();
        list1.add("1");
        list1.add("1");
        list1.add("1");
        list1.add("1");
        redisService.setList("terminal:list1:123", list1);

        printResult(redisService.getList("terminal:list1:123", String.class));

        List<String> list2 = new ArrayList<>();
        list2.add("2");
        list2.add("2");
        list2.add("2");
        list2.add("2");
        redisService.setList("terminal:asd1:123", list2);

        printResult(redisService.getList("terminal:asd1:123", String.class));


        redisService.setValue("terminal:asd2:2341", 2341, 1, TimeUnit.MINUTES);
        redisService.setValue("terminal:asd3:sdfsd", "sdfsd", 1, TimeUnit.MINUTES);
        redisService.setValue("terminal:asd4:1242431", 1242431, 1, TimeUnit.MINUTES);
        redisService.setValue("terminal:asd5:1223424331", 1223424331, 1, TimeUnit.MINUTES);


    }

    @Test
    void testSetValue() {
    }

    @Test
    void testSetValue1() {
    }

    @Test
    void reverseRange() {
//        RedisCacheManager.getCache(TERMINAL).put("as1131d", "sdad");
        redisService.setValue("terminal:asd1:123", 123, 1, TimeUnit.MINUTES);
        redisService.setValue("terminal:asd2:2341", 2341, 1, TimeUnit.MINUTES);
        redisService.setValue("terminal:asd3:sdfsd", "sdfsd", 1, TimeUnit.MINUTES);
        redisService.setValue("terminal:asd4:1242431", 1242431, 1, TimeUnit.MINUTES);
        redisService.setValue("terminal:asd5:1223424331", 1223424331, 1, TimeUnit.MINUTES);
    }

    @Test
    void range() {
        redisService.setValue("aa", "aa", 1, TimeUnit.HOURS);
    }

    @Test
    void getValue() {
        int i = redisService.getValue("APAAS_UNREAD_MSG_COUNT_05c60924-e7d4-49e0-bfeb-15864b615176", Integer.class);
        System.out.println("i____________" + i);
    }

    @Test
    void addZset() {
    }

    @Test
    void testAddZset() {
        String key = "comment_id_cache_1";
    /*    DefaultTypedTuple<String> tuple2 = new DefaultTypedTuple<String>("p2", 2.1);
        DefaultTypedTuple<String> tuple3 = new DefaultTypedTuple<String>("p3", 3.1);
        DefaultTypedTuple<String> tuple4 = new DefaultTypedTuple<String>("p4", 4.1);
        DefaultTypedTuple<String> tuple5 = new DefaultTypedTuple<String>("p5", 5.1);
        DefaultTypedTuple<String> tuple6 = new DefaultTypedTuple<String>("p6", 6.1);
        DefaultTypedTuple<String> tuple7 = new DefaultTypedTuple<String>("p7", 7.1);*/
//        redisService.addZSet(key,Arrays.asList("p1", "p2", "p3", "p4", "p5", "p6"));


        redisService.addZSet("terminal:asd1:123", "123", 1d, 1);
        redisService.addZSet("terminal:asd1:123", "1asdad", 1d, 1);
        redisService.addZSet("terminal:asd1:123", "23423", 1d, 1);


        redisService.addZSet("terminal:asd1:123", "123", 1d, 1);
        redisService.addZSet("terminal:asd1:123", "1asdad", 1d, 1);
        redisService.addZSet("terminal:asd1:123", "23423", 1d, 1);

    }

    @Test
    void removeZset() {

        redisService.setValue("team:sada:2asda21232:readConfirm", "ddddddddddddd", 1l);

    }


    @Test
    void getZSetSize() {
        QueryWrapper<ChatMsg> queryWrapper = new QueryWrapper();
        queryWrapper.select("message_id,created_time")
                .last("limit " + 10)
                .orderByDesc("created_time").lambda()
                .eq(ChatMsg::getGroupId, "ce957cd8ccd2428398c26e3ae1ffb75c")
                .lt(ChatMsg::getCreatedTime, DateUtil.date(System.currentTimeMillis()));
        List<ChatMsg> chatMsgList = chatMsgService.list(queryWrapper);
        List<String> idList = chatMsgList.stream().map(ChatMsg::getMessageId).collect(Collectors.toList());
        for (ChatMsg chatMsg : chatMsgList) {
            DefaultTypedTuple<String> tuple2;
            tuple2 = new DefaultTypedTuple<String>(chatMsg.getMessageId(), Double.valueOf(chatMsg.getCreatedTime().getTime()));
        }
    }


    @Test
    public void addListToZset() {

// 获取 RedisTemplate 对象
// 假设你的 List<String> 名称为 list，有序集合的名称为 zset
        List<String> list = Arrays.asList("item1", "item2", "item3");

// 获取 ZSetOperations 对象
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();

// 使用 ZSetOperations 的 add 方法将 List 的元素添加到有序集合中
        for (String item : list) {
            zSetOperations.add("zset", item, 0);  // 这里将分数设为 0
        }

    }


    public void batchIncrement(Map<String, Integer> keyValues) {
        ValueOperations<String, String> valueOps = redisTemplate.opsForValue();

        Map<String, String> stringKeyValues = new HashMap<>();
        for (Map.Entry<String, Integer> entry : keyValues.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            stringKeyValues.put(key, String.valueOf(value));
        }

        valueOps.multiSet(stringKeyValues);

        // Increment values
        for (Map.Entry<String, Integer> entry : keyValues.entrySet()) {
            String key = entry.getKey();
            Integer incrementBy = entry.getValue();
            valueOps.increment(key, incrementBy);
        }

        redisTemplate.exec();
    }

    @Test
    public void batchIncrement() {
        // Initialize RedisTemplate
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        // Set up RedisTemplate properties


        Map<String, Integer> keyValues = new HashMap<>();
        keyValues.put("key1", 5);
        keyValues.put("key2", 10);
        // Add more key-value pairs

        batchIncrement(keyValues);
    }



    @Test
    public void Increment() {
        // Initialize RedisTemplate
        redisTemplate.opsForValue().increment("aa", 1);
        printResult(redisTemplate.opsForValue().get("aa"));
    }


    @Test
    public void batchRemove() {

        final String MSG_HELPER_QUEUE = "MSG_HELPER_QUEUE";
//        for (int i = 0; i < 5; i++) {
        String jsonObject = "{\"convType\":0,\"fromType\":\"apaas\",\"mentionedType\":0,\"messageId\":\"f8cb5db0a1df4af1ab1184e62885e519\",\"pushContent\":\"您有一项待办：《FZ2023100003 KMCPL 2023-10 2023-10 汇总付款申请[Summary Of Payment Application] RMB 306965.83 2023-10-17 11:44:37》，请您及时处理\",\"pushData\":{\"chatType\":2,\"concatTitle\":\"【流程待办】您有一项待办：《FZ2023100003 KMCPL 2023-10 2023-10 汇总付款申请[Summary Of Payment Application] RMB 306965.83 2023-10-17 11:44:37》，请您及时处理\",\"content\":\"<html><br><!DOCTYPE html><html><head><meta name=\\\"viewport\\\" content=\\\"width=device-width,initial-scale=1.0 ,maximum-scale=1, minimum-scale=1.0,user-scalable=no\\\" /></head><body><p>曾明怡.ZengMingYi，您好<br/>        许彬彬.XuBinBin刚刚制单了许彬彬.XuBinBin的付款汇总申请《FZ2023100003 KMCPL 2023-10 2023-10 汇总付款申请[Summary Of Payment Application] RMB 306965.83 2023-10-17 11:44:37》，现到达结算经理环节，请点此<a target=\\\"_blank\\\" href=\\\"https://oi.bwoil.com/portal/#/smartForm?needTransfer=need&processID=c58fbcde121c4df2845693470e688359&processDefNo=Process_fin_fukuanshenqing_huizong&processInstId=7ed9645a-6c9f-11ee-9368-0242ba9e91e8&taskInstId=7f986e9d-6c9f-11ee-adaf-02422538bde3&taskStatus=1\\\">处理</a></p><p><br/></p><p>--以上消息由智能办公系统自动发送<br/></p></body></html><br></html>\",\"id\":\"f8cb5db0a1df4af1ab1184e62885e519\",\"linkList\":[{\"linkName\":\"处理\",\"linkUrl\":\"https://oi.bwoil.com/portal/#/smartForm?needTransfer=noneed&processID=c58fbcde121c4df2845693470e688359&processDefNo=Process_fin_fukuanshenqing_huizong&processInstId=7ed9645a-6c9f-11ee-9368-0242ba9e91e8&taskInstId=7f986e9d-6c9f-11ee-adaf-02422538bde3&taskStatus=1\"}],\"msgClass\":\"approveMsg\",\"receiverId\":\"7806af91-615c-42fd-b136-34e1db7ce9d3\",\"scene\":\"processToDo\",\"title\":\"您有一项待办：《FZ2023100003 KMCPL 2023-10 2023-10 汇总付款申请[Summary Of Payment Application] RMB 306965.83 2023-10-17 11:44:37》，请您及时处理\"},\"pushMessageType\":\"NORMAL\",\"senderName\":\"快鹭智能办公\",\"unReceivedMsgNumber\":4,\"unReceivedMsgNumberOfPad\":4,\"userId\":\"7806af91-615c-42fd-b136-34e1db7ce9d3\"}";
        PushMessage pushMessage = JSONObject.toJavaObject(JSONObject.parseObject(jsonObject), PushMessage.class);
        redisService.addZSet(MSG_HELPER_QUEUE, pushMessage, Long.valueOf(new Date().getTime()).doubleValue(), 10, TimeUnit.MINUTES);
//        }
        List<PushMessage> pushMessageList = redisService.range(MSG_HELPER_QUEUE, 0, 9, PushMessage.class);
//        Long removeCount = redisService.removeZSets(MSG_HELPER_QUEUE, pushMessageList);
//        printResult(pushMessageList);
    }


    @Test
    public void addToPushQueue() {
        final String MSG_HELPER_QUEUE = "MSG_HELPER_QUEUE";
        String jsonObject = "{\"convType\":0,\"fromType\":\"apaas\",\"mentionedType\":0,\"messageId\":\"f8cb5db0a1df4af1ab1184e62885e519\",\"pushContent\":\"您有一项待办：《FZ2023100003 KMCPL 2023-10 2023-10 汇总付款申请[Summary Of Payment Application] RMB 306965.83 2023-10-17 11:44:37》，请您及时处理\",\"pushData\":{\"chatType\":2,\"concatTitle\":\"【流程待办】您有一项待办：《FZ2023100003 KMCPL 2023-10 2023-10 汇总付款申请[Summary Of Payment Application] RMB 306965.83 2023-10-17 11:44:37》，请您及时处理\",\"content\":\"<html><br><!DOCTYPE html><html><head><meta name=\\\"viewport\\\" content=\\\"width=device-width,initial-scale=1.0 ,maximum-scale=1, minimum-scale=1.0,user-scalable=no\\\" /></head><body><p>曾明怡.ZengMingYi，您好<br/>        许彬彬.XuBinBin刚刚制单了许彬彬.XuBinBin的付款汇总申请《FZ2023100003 KMCPL 2023-10 2023-10 汇总付款申请[Summary Of Payment Application] RMB 306965.83 2023-10-17 11:44:37》，现到达结算经理环节，请点此<a target=\\\"_blank\\\" href=\\\"https://oi.bwoil.com/portal/#/smartForm?needTransfer=need&processID=c58fbcde121c4df2845693470e688359&processDefNo=Process_fin_fukuanshenqing_huizong&processInstId=7ed9645a-6c9f-11ee-9368-0242ba9e91e8&taskInstId=7f986e9d-6c9f-11ee-adaf-02422538bde3&taskStatus=1\\\">处理</a></p><p><br/></p><p>--以上消息由智能办公系统自动发送<br/></p></body></html><br></html>\",\"id\":\"f8cb5db0a1df4af1ab1184e62885e519\",\"linkList\":[{\"linkName\":\"处理\",\"linkUrl\":\"https://oi.bwoil.com/portal/#/smartForm?needTransfer=noneed&processID=c58fbcde121c4df2845693470e688359&processDefNo=Process_fin_fukuanshenqing_huizong&processInstId=7ed9645a-6c9f-11ee-9368-0242ba9e91e8&taskInstId=7f986e9d-6c9f-11ee-adaf-02422538bde3&taskStatus=1\"}],\"msgClass\":\"approveMsg\",\"receiverId\":\"7806af91-615c-42fd-b136-34e1db7ce9d3\",\"scene\":\"processToDo\",\"title\":\"您有一项待办：《FZ2023100003 KMCPL 2023-10 2023-10 汇总付款申请[Summary Of Payment Application] RMB 306965.83 2023-10-17 11:44:37》，请您及时处理\"},\"pushMessageType\":\"NORMAL\",\"senderName\":\"快鹭智能办公\",\"unReceivedMsgNumber\":4,\"unReceivedMsgNumberOfPad\":4,\"userId\":\"7806af91-615c-42fd-b136-34e1db7ce9d3\"}";
        msgHelperScheduledTask.addToPushQueue(jsonObject);
    }
}