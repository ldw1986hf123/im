//package com.kuailu.im.server.schduler;
//
//import cn.hutool.json.JSONUtil;
//import com.alibaba.fastjson.JSONObject;
//import com.google.gson.JsonObject;
//import com.kuailu.im.core.packets.ChatType;
//import com.kuailu.im.core.packets.ValidStatus;
//import com.kuailu.im.server.command.handler.IatReqHandler;
//import com.kuailu.im.server.enums.YesOrNoEnum;
//import com.kuailu.im.server.model.entity.ChatGroup;
//import com.kuailu.im.server.model.entity.ChatMsg;
//import com.kuailu.im.server.mq.IatParam;
//import com.kuailu.im.server.mq.PushMessage;
//import com.kuailu.im.server.service.IChatGroupService;
//import com.kuailu.im.server.service.IChatMsgService;
//import com.kuailu.im.server.service.IConversationService;
//import com.kuailu.im.server.util.RedisService;
//import com.kuailu.im.server.util.UUIDUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections4.CollectionUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.time.Instant;
//import java.util.*;
//
//import static com.kuailu.im.server.command.handler.IatReqHandler.userIdToWebsocket;
//
///**
// * 只有群消息是定时推送
// */
//@Component
//@Slf4j
//public class IatScheduledTask {
//    private final String IAT_HELPER_QUEUE = "IAT_HELPER_QUEUE";
//    @Autowired
//    RedisService redisService;
//
//    @Autowired
//    IChatMsgService msgService;
//
//
//    private Set<String> iatUserIdList = new HashSet<>();
//
//
//    @Async
//    @Scheduled(fixedRate = 1000) // 每隔 1 秒执行一次，取十个数据出来保存
//    private void saveToMsg() throws InterruptedException {
//        Long lastTime = redisService.getValue(IatReqHandler.USER_LAST_BYTE_TIME, Long.class);
//        if (null==lastTime){
//            return;
//        }
//        for (String key : IatReqHandler.userIdToLastTime.keySet() ){
//           log.info("定时器运行------------------");
//            if (System.currentTimeMillis() - lastTime > 1000) {
//                log.info("1秒了都没有数据，关闭socket-, Value: " + IatReqHandler.userIdToLastTime.get(key));
//                log.info("userId :{} 过期", key);
//                log.info("  end----------------------------------");
//                JsonObject frame2 = new JsonObject();
//                JsonObject data2 = new JsonObject();
//                data2.addProperty("status", 2);
//                data2.addProperty("audio", "");
//                data2.addProperty("format", "audio/L16;rate=16000");
//                data2.addProperty("encoding", "raw");
//                frame2.add("data", data2);
//                log.info("all data is send");
//                userIdToWebsocket.get(key).send(frame2.toString());
//                userIdToWebsocket.remove(key);
//            }
//        }
//
//    }
//
//    public void addToPushQueue(String iatByteStr) {
//        Date now = Date.from(Instant.now());
//        IatParam iatParam = JSONUtil.toBean(iatByteStr, IatParam.class);
//        String userId = iatParam.getUserId();
//        iatUserIdList.add(userId);
//    }
//}