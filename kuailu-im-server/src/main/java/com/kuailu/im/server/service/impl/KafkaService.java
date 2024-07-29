package com.kuailu.im.server.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kuailu.im.core.common.PublicRedisKey;
import com.kuailu.im.core.packets.ChatType;
import com.kuailu.im.server.enums.MessageOperaTypeEnum;
import com.kuailu.im.server.enums.MessageTypeEnum;
import com.kuailu.im.server.model.entity.*;
import com.kuailu.im.server.mq.PushMessage;
import com.kuailu.im.server.mq.PushMessageType;
import com.kuailu.im.server.service.*;
import com.kuailu.im.server.util.ApplicationContextHelper;
import com.kuailu.im.server.util.RedisService;
import com.kuailu.im.server.vo.ChatGroupDetailVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class KafkaService {
    @Autowired
    KafkaTemplate kafkaTemplate;


    @Autowired
    IChatGroupService groupService;

    @Autowired
    RedisService redisService;

    @Autowired
    IConversationService conversationService;


    public void putPrivateKafkaPush(ChatMsg chatMsg, String conversationId) {
        try {
            String msg = chatMsg.getMsg();
            String receiver = chatMsg.getReceiver();
            if (StringUtils.isEmpty(msg)) {
                log.error("msg 为空，不推送.chatMsg:{}", JSONUtil.toJsonStr(chatMsg));
                return;
            }
            Integer msgType = chatMsg.getMsgType();
            String senderName = chatMsg.getSenderName();
            String pushContent = JSONUtil.parseObj(msg).getStr("content");
            if (MessageTypeEnum.MERGE_REDIRECT.getCode() == msgType) {
                if (ChatType.CHAT_TYPE_PUBLIC.getNumber() == chatMsg.getChatType()) {
                    pushContent = senderName + ":[会话记录]";
                }
                if (ChatType.CHAT_TYPE_PRIVATE.getNumber() == chatMsg.getChatType()) {
                    pushContent = "[会话记录]";
                }

            }
            //pushData 就是 bizData
            Map<String, Object> pushData = new HashMap();
            PushMessage pushMessage = new PushMessage();
            pushData.put("senderName", senderName);
            pushMessage.setSenderName(senderName);
            pushMessage.setPushContent(pushContent);

            if (MessageTypeEnum.AUDIO.getCode() == msgType ||
                    MessageTypeEnum.VIDEO.getCode() == msgType ||
                    MessageTypeEnum.PICTURE.getCode() == msgType ||
                    MessageTypeEnum.FILE.getCode() == msgType) {
                pushMessage.setPushContent("[" + MessageTypeEnum.getInfoByType(msgType) + "]");
            }
            pushMessage.setMentionedType(0);
            //pushData 就是 bizData
            pushData.put("senderName", senderName);
            pushData.put("senderId", chatMsg.getSender());
            pushData.put("conversationId", conversationId);
            pushData.put("groupId", chatMsg.getGroupId());
            pushData.put("sender", chatMsg.getSender());
            pushData.put("pushContent", pushContent);
            pushData.put("receiverId", receiver);
            pushData.put("msgId", chatMsg.getMessageId());
            pushData.put("conversationName", senderName);
            pushMessage.setPushData(JSONUtil.toJsonStr(pushData));
            pushMessage.setSender(chatMsg.getSender());


            buildPushUnReadMsgCount(pushMessage, receiver);

            pushMessage.setConvType(chatMsg.getChatType());
            pushMessage.setPushMessageType(PushMessageType.NORMAL);
            pushMessage.setUserId(receiver);
            pushMessage.setMessageId(chatMsg.getMessageId());
            String kafkaJsonStr = JSONUtil.toJsonStr(pushMessage);
            kafkaTemplate.send("push-service", IdUtil.fastUUID(), kafkaJsonStr);
        } catch (Exception e) {
            log.error("发消息推送kafka异常,chatMsg:{}", JSONUtil.toJsonStr(chatMsg), e);
        }
    }

    public void putPublicKafkaPush(ChatMsg chatMsg, String receiver, String conversationId, String groupName) {
        try {
            String msg = chatMsg.getMsg();
            Integer msgType = chatMsg.getMsgType();
            String senderName = chatMsg.getSenderName();

            String pushContent = "";

            //pushData 就是 bizData
            Map<String, Object> pushData = new HashMap();
            PushMessage pushMessage = new PushMessage();
            pushMessage.setSenderName(groupName);

            if (MessageTypeEnum.MERGE_REDIRECT.getCode() == msgType) {
                pushContent = "[会话记录]";
            } else if (MessageTypeEnum.AUDIO.getCode() == msgType ||
                    MessageTypeEnum.VIDEO.getCode() == msgType ||
                    MessageTypeEnum.PICTURE.getCode() == msgType ||
                    MessageTypeEnum.FILE.getCode() == msgType) {
                pushContent = ("[" + MessageTypeEnum.getInfoByType(msgType) + "]");
            } else {
                pushContent = JSONUtil.parseObj(msg).getStr("content");
            }

            pushMessage.setPushContent(senderName + ":" + pushContent);
            pushMessage.setMentionedType(0);
            /********************pushData 就是 bizData**********************/
            pushData.put("senderName", senderName);
            pushData.put("senderId", chatMsg.getSender());
            pushData.put("conversationId", conversationId);
            pushData.put("groupId", chatMsg.getGroupId());
            pushData.put("sender", chatMsg.getSender());

            pushData.put("pushContent", pushContent);
            pushData.put("receiverId", receiver);
            pushData.put("msgId", chatMsg.getMessageId());

            pushData.put("conversationName", senderName);
            pushMessage.setPushData(JSONUtil.toJsonStr(pushData));
            /********************pushData 就是 bizData**********************/


            pushMessage.setSender(chatMsg.getSender());
            pushMessage.setUnReceivedMsgNumber(0);
            pushMessage.setConvType(chatMsg.getChatType());
            pushMessage.setPushMessageType(PushMessageType.NORMAL);
            pushMessage.setUserId(receiver);
            pushMessage.setMessageId(chatMsg.getMessageId());

            buildPushUnReadMsgCount(pushMessage, receiver);

            String kafkaJsonStr = JSONUtil.toJsonStr(pushMessage);
            kafkaTemplate.send("push-service", IdUtil.fastUUID(), kafkaJsonStr);
        } catch (Exception e) {
            log.error("发消息推送kafka异常,chatMsg:{}", JSONUtil.toJsonStr(chatMsg), e);
        }
    }

    public void revoke(List<String> receiverList, ChatMsg chatMsg, String groupId, String conversationId) {
       /* List<String> receiverList = new ArrayList<>();
        if (ChatType.CHAT_TYPE_PRIVATE.getNumber() == chatMsg.getChatType()) {
            receiverList.add(chatMsg.getReceiver());
        } else {
            List<ChatGroupMember> groupMemberList = groupMemberService.getAllMembers(chatMsg.getGroupId());
            receiverList = groupMemberList.stream().map(ChatGroupMember::getUserId).collect(Collectors.toList());
        }*/
        ChatGroup chatGroup = groupService.getOne(new QueryWrapper<ChatGroup>().select("group_name").lambda().eq(ChatGroup::getGroupId, groupId));
        pushToKafka(chatMsg, receiverList, PushMessageType.RECALLED, chatGroup.getGroupName(), conversationId);
    }


    private void pushToKafka(ChatMsg chatMsg, List<String> receiverList, PushMessageType pushMessageType, String groupName, String conversationId) {
        log.info("撤回推送开始");
        try {
            for (String receiver : receiverList) {
                String msg = chatMsg.getMsg();

                if (StringUtils.isEmpty(msg)) {
                    log.error("msg 为空，不推送.chatMsg:{}", JSONUtil.toJsonStr(chatMsg));
                    return;
                }
                Integer msgType = chatMsg.getMsgType();
                String senderName = chatMsg.getSenderName();

                String pushContent = JSONUtil.parseObj(msg).getStr("content");
                //pushData 就是 bizData
                Map<String, Object> pushData = new HashMap();
                PushMessage pushMessage = new PushMessage();
                if (ChatType.CHAT_TYPE_PRIVATE.getNumber() == chatMsg.getChatType()) {
                    pushData.put("senderName", senderName);
                    pushMessage.setSenderName(senderName);
                    pushMessage.setPushContent(pushContent);
                } else if (ChatType.CHAT_TYPE_PUBLIC.getNumber() == chatMsg.getChatType()) {
                 /*   ChatGroupDetailVo chatGroupDetailVo = groupService.getDetailsByGroupId(chatMsg.getGroupId());
                    if (chatGroupDetailVo != null) {*/
                    pushMessage.setSenderName(groupName);
                    pushMessage.setPushContent(senderName + ":" + pushContent);
//                    }
                }
                if (MessageTypeEnum.AUDIO.getCode() == msgType ||
                        MessageTypeEnum.VIDEO.getCode() == msgType ||
                        MessageTypeEnum.PICTURE.getCode() == msgType ||
                        MessageTypeEnum.FILE.getCode() == msgType) {
                    pushMessage.setPushContent("[" + MessageTypeEnum.getInfoByType(msgType) + "]");
                }
                pushMessage.setMentionedType(0);
//                Conversation conversation = conversationService.getOne(new LambdaQueryWrapper<Conversation>().eq(Conversation::getChatgroupId, chatMsg.getGroupId()));
//                NoDisturb noDisturb = noDisturbService.getOne(receiver, conversation.getConversationId());
//                if (!ObjectUtils.isEmpty(noDisturb)) {
//                    return;
//                }
                /********************pushData 就是 bizData**********************/
                pushData.put("senderName", senderName);
                pushData.put("senderId", chatMsg.getSender());
                pushData.put("conversationId", conversationId);
                pushData.put("groupId", chatMsg.getGroupId());
                pushData.put("sender", chatMsg.getSender());

                pushData.put("pushContent", pushContent);
                pushData.put("receiverId", receiver);
                pushData.put("msgId", chatMsg.getMessageId());

                pushData.put("conversationName", senderName);
                pushMessage.setPushData(JSONUtil.toJsonStr(pushData));
                /********************pushData 就是 bizData**********************/

                pushMessage.setSender(chatMsg.getSender());
                pushMessage.setUnReceivedMsgNumber(0);
                pushMessage.setConvType(chatMsg.getChatType());
                pushMessage.setPushMessageType(pushMessageType);
                pushMessage.setUserId(receiver);
                pushMessage.setMessageId(chatMsg.getMessageId());

                String kafkaJsonStr = JSONUtil.toJsonStr(pushMessage);
//                log.info("放到kafka中的数据：{}", kafkaJsonStr);
                kafkaTemplate.send("push-service", IdUtil.fastUUID(), kafkaJsonStr);
            }
        } catch (Exception e) {
            log.error("发消息推送kafka异常,chatMsg:{}", JSONUtil.toJsonStr(chatMsg), e);
        }
        log.info("撤回推送完成receiverList：{}  ", JSONUtil.toJsonStr(receiverList));
        log.info("撤回推送完成  chatMsg:{}", JSONUtil.toJsonStr(chatMsg));
    }


    /**
     * app：APAAS_UNREAD_MSG_COUNT_ + im消息
     * pad：APAAS_PAD_UNREAD_MSG_COUNT_
     *
     * @param pushMessage
     * @param receiver
     */
    private void buildPushUnReadMsgCount(PushMessage pushMessage, String receiver) {
        /***************************这种共享redis的方式真TMSB***************************/
        Integer imUnReadCount = conversationService.getTotalUnReadMsgCount(receiver);

        String appKEy = PublicRedisKey.APAAS_UNREAD_MSG_COUNT + receiver;
        String apaasPadKey = PublicRedisKey.APAAS_PAD_UNREAD_MSG_COUNT + receiver;

        Integer appUnReadCount = redisService.getValue(appKEy, Integer.class) == null ? 0 : redisService.getValue(appKEy, Integer.class);
        Integer padUnRead = redisService.getValue(apaasPadKey, Integer.class) == null ? 0 : redisService.getValue(apaasPadKey, Integer.class);
        log.info("receiver:{}   appUnReadCount:{},  padUnRead:{}   imUnReadCount:{}", receiver, appUnReadCount, padUnRead, imUnReadCount);

        pushMessage.setUnReceivedMsgNumber(appUnReadCount + imUnReadCount);
        pushMessage.setUnReceivedMsgNumberOfPad(padUnRead);
        /***************************这种共享redis的方式真TMSB***************************/
    }


}
