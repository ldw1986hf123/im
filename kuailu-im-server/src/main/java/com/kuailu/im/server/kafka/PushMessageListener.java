package com.kuailu.im.server.kafka;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.kuailu.im.core.packets.ChatType;
import com.kuailu.im.core.packets.ValidStatus;
import com.kuailu.im.server.enums.YesOrNoEnum;
import com.kuailu.im.server.model.entity.ChatGroup;
import com.kuailu.im.server.model.entity.ChatMsg;
import com.kuailu.im.server.mq.PushMessage;
import com.kuailu.im.server.schduler.MsgHelperScheduledTask;
import com.kuailu.im.server.schduler.PushScheduledTask;
import com.kuailu.im.server.service.IChatGroupService;
import com.kuailu.im.server.service.IChatMsgService;
import com.kuailu.im.server.service.IConversationService;
import com.kuailu.im.server.service.INoDisturbService;
import com.kuailu.im.server.service.impl.KafkaService;
import com.kuailu.im.server.util.RedisService;
import com.kuailu.im.server.util.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class PushMessageListener {

    @Autowired
    RedisService redisService;

    @Autowired
    IChatGroupService chatGroupService;

    @Autowired
    IChatMsgService chatMsgService;

    @Autowired
    IConversationService conversationService;


    @Autowired
    MsgHelperScheduledTask msgHelperScheduledTask;

    /**
     * 最好能让kafka一个个顺序进行消费，避免带来并发问题
     * （1）可以通过配置 max.poll.records 属性来控制每次拉取的消息数量。默认情况下，它是 500 条。如果你只想拉取一条消息，可以将其设置为 1。
     * spring.kafka.consumer.properties.max.poll.records: 1
     * 这样配置后，每次 poll() 方法调用将只拉取一条消息。
     * 请注意，逐个拉取消息可能会导致较高的延迟，因此你需要权衡消费效率和实时性之间的需求。
     * （2）
     *
     * @param consumerRecords
     * @param ack
     */
    @KafkaListener(
            topics = "#{'${spring.kafka.consumer.topics}'.split(',')}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void batchConsumer(List<ConsumerRecord<String, String>> consumerRecords, Acknowledgment ack) {
        try {

            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                String jsonObject = consumerRecord.value();
                log.info("接收到kafka推送pushMessage：{}", jsonObject);

//                PushMessage pushMessage = JSONObject.toJavaObject(JSONObject.parseObject(jsonObject), PushMessage.class);

                //先放到redis中，然后一个个去除操作
//                msgHelperScheduledTask.addToPushQueue(jsonObject);
          /*

                String receiver = pushMessage.getUserId();
                String msgId = UUIDUtil.getUUID();

                ChatGroup chatGroup = chatGroupService.createMsgHelperGroup(receiver);
                chatGroupService.createMsgHelperGroupMember(chatGroup);
                chatGroupService.createMsgHelperConversation(receiver, chatGroup.getGroupId());

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
                chatMsgService.save(chatMsg);


                */
            }
        } catch (Exception e) {
            log.error("消费apass kafka消息异常", e);
        } finally {
            ack.acknowledge();
        }
    }

}
