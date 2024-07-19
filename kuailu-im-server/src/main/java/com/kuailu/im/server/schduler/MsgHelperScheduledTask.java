package com.kuailu.im.server.schduler;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kuailu.im.core.packets.ChatType;
import com.kuailu.im.core.packets.ValidStatus;
import com.kuailu.im.server.enums.MessageTypeEnum;
import com.kuailu.im.server.enums.YesOrNoEnum;
import com.kuailu.im.server.model.entity.ChatGroup;
import com.kuailu.im.server.model.entity.ChatGroupMember;
import com.kuailu.im.server.model.entity.ChatMsg;
import com.kuailu.im.server.mq.PushMessage;
import com.kuailu.im.server.service.*;
import com.kuailu.im.server.service.impl.KafkaService;
import com.kuailu.im.server.util.RedisService;
import com.kuailu.im.server.util.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
@Deprecated
public class MsgHelperScheduledTask {
    private final String MSG_HELPER_QUEUE = "MSG_HELPER_QUEUE";
    @Autowired
    RedisService redisService;

    @Autowired
    IChatGroupService groupService;

    @Autowired
    IChatMsgService msgService;

    @Autowired
    IConversationService conversationService;


/*    @Async
    @Scheduled(fixedRate = 30000)*/ // 每隔 30 秒执行一次，取十个数据出来保存
    private void saveToMsg() {
        List<String> pushMessageStrList = redisService.range(MSG_HELPER_QUEUE, 0, 9, String.class);

        if (CollectionUtils.isEmpty(pushMessageStrList)) {
            log.debug("没有提示消息要保存");
            return;
        }
        List<String> saveMessageId = new ArrayList();
        Date now = Date.from(Instant.now());
        List chatMsgList = new ArrayList<>();
        for (String pushMessageStr : pushMessageStrList) {
            try {
                PushMessage pushMessage = JSONUtil.toBean(pushMessageStr, PushMessage.class);
                saveMessageId.add(pushMessage.getMessageId());


                String receiver = pushMessage.getUserId();
                String msgId = UUIDUtil.getUUID();

                //todo 可以放到登录的地方做，不用再这里频繁做
                ChatGroup chatGroup = groupService.createMsgHelperGroup(receiver);
                groupService.createMsgHelperGroupMember(chatGroup);
                groupService.createMsgHelperConversation(receiver, chatGroup.getGroupId());

                ChatMsg chatMsg = new ChatMsg();
                chatMsg.setMessageId(msgId);
                chatMsg.setChatType(ChatType.CHAT_TYPE_MSG_HELPER.getNumber());
                chatMsg.setSender(chatGroup.getGroupId());
                chatMsg.setSenderName("消息助手");
                String conversationId = conversationService.getConversationIdByGroupId(chatGroup.getGroupId(), receiver);
                chatMsg.setConversationId(conversationId);
                chatMsg.setReceiver(receiver);
                chatMsg.setMsgType(6);
                chatMsg.setCreatedTime(now);
                chatMsg.setSendTime(now);
                chatMsg.setGroupId(chatGroup.getGroupId());
                chatMsg.setStatus(ValidStatus.NORMAL.getValue());
                chatMsg.setIsRead(YesOrNoEnum.NO.getCode());

                String pushData = pushMessage.pushData;
                JSONObject jsonObject1 = JSONObject.parseObject(pushData);
                jsonObject1.put("groupId", chatGroup.getGroupId());
                jsonObject1.put("chatType", ChatType.CHAT_TYPE_MSG_HELPER.getNumber());
                pushMessage.setPushData(jsonObject1.toJSONString());
                String concatTitle = jsonObject1.getString("concatTitle");

                JSONObject jsonObject2 = new JSONObject();
                jsonObject2.put("content", concatTitle);
                chatMsg.setMsg(jsonObject2.toJSONString());
                chatMsg.setMsgContent(concatTitle);
                chatMsgList.add(chatMsg);
            } catch (Exception e) {
                log.error("提示消息保存失败pushMessageStr:{}", pushMessageStr, e);
            }
        }
        msgService.saveBatch(chatMsgList);
        Long removeCount = redisService.removeZSets(MSG_HELPER_QUEUE, pushMessageStrList);
        log.info("提示消息保存完成 saveMessageId:{}, removeCount:{}", JSONUtil.toJsonStr(saveMessageId), removeCount);
    }

    public void addToPushQueue(String pushMessageStr) {
        Date now = Date.from(Instant.now());
        redisService.addZSet(MSG_HELPER_QUEUE, pushMessageStr, Long.valueOf(now.getTime()).doubleValue(), 10, TimeUnit.MINUTES);
    }
}