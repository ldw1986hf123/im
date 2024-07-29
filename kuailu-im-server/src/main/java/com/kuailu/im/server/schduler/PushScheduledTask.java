package com.kuailu.im.server.schduler;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kuailu.im.core.packets.ChatType;
import com.kuailu.im.server.enums.MessageTypeEnum;
import com.kuailu.im.server.model.entity.ChatGroup;
import com.kuailu.im.server.model.entity.ChatGroupMember;
import com.kuailu.im.server.model.entity.ChatMsg;
import com.kuailu.im.server.service.IAtMsgService;
import com.kuailu.im.server.service.IChatGroupMemberService;
import com.kuailu.im.server.service.IChatGroupService;
import com.kuailu.im.server.service.INoDisturbService;
import com.kuailu.im.server.service.impl.KafkaService;
import com.kuailu.im.server.util.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 只有群消息是定时推送
 */
@Component
@Slf4j
public class PushScheduledTask {
    private final String PUSH_GROUP_MSG_QUEUE = "PUSH_GROUP_MSG_QUEUE";

    @Autowired
    KafkaService kafkaService;

    @Autowired
    RedisService redisService;

    @Autowired
    IAtMsgService atMsgService;


    @Autowired
    IChatGroupService groupService;

    @Autowired
    INoDisturbService noDisturbService;

    @Autowired
    IChatGroupMemberService groupMemberService;

    //todo 每次都被执行了两次，要解决
    @Async
    @Scheduled(fixedRate = 30000) // 每隔 30 秒执行一次
    public void scheduledTask() {
        //每次推送
        List<ChatMsg> chatMsgList = redisService.range(PUSH_GROUP_MSG_QUEUE, 0, 9, ChatMsg.class);
        if (CollectionUtils.isEmpty(chatMsgList)) {
            return;
        }

        List<String> groupIdList = chatMsgList.stream().map(ChatMsg::getGroupId).collect(Collectors.toList());
        List<ChatGroup> chatGroupList = groupService.list(new QueryWrapper<ChatGroup>().select("group_id", "group_name").lambda().in(ChatGroup::getGroupId, groupIdList));
        Map<String, ChatGroup> groupIdToGroup = chatGroupList.stream().collect(Collectors.toMap(ChatGroup::getGroupId, ChatGroup -> ChatGroup));


        for (ChatMsg chatMsg : chatMsgList) {
            String groupId = chatMsg.getGroupId();
            Integer chatType = chatMsg.getChatType();
            Integer messageType = chatMsg.getMsgType();
            List<ChatGroupMember> groupMemberList = groupMemberService.getAllMembers(groupId);
            ChatGroup chatGroup = groupIdToGroup.get(groupId);
            if (null == chatGroup) {
                log.error("数据有误，groupId :{} 找不到对应的群", groupId);
                continue;
            }
            String groupName = chatGroup.getGroupName();
            List<String> receiverUserIdList = groupMemberList.stream().map(ChatGroupMember::getUserId).collect(Collectors.toList());

            String conversationId = chatMsg.getConversationId();
            List<String> noDisturbUserIdList = noDisturbService.getNoDisturbUserId(conversationId);
            List<String> atUserIdList = new ArrayList<>();
            if (MessageTypeEnum.ATMessage.getCode() == messageType) {
                atUserIdList = atMsgService.extractAtUserIdInMag(chatMsg, groupId);
            }

            if (CollectionUtils.isNotEmpty(noDisturbUserIdList)) {
                //@消息，不收免打扰影响
                noDisturbUserIdList.removeAll(atUserIdList);
            }
            receiverUserIdList.removeAll(noDisturbUserIdList);
            //不要给自己发推送
            receiverUserIdList.remove(chatMsg.getSender());

            for (String receiver : receiverUserIdList) {
                if (ChatType.CHAT_TYPE_PUBLIC.getNumber() == chatType) {
                    kafkaService.putPublicKafkaPush(chatMsg, receiver, chatMsg.getConversationId(), groupName);
                } else {
                    kafkaService.putPrivateKafkaPush(chatMsg, conversationId);
                }
            }
        }
        List<String> pushedMsgIdList = chatMsgList.stream().map(ChatMsg::getMessageId).collect(Collectors.toList());

        Long removeCount = redisService.removeZSets(PUSH_GROUP_MSG_QUEUE, chatMsgList);
        log.info("推送完成 msgId:{},removeCount:{}", JSONUtil.toJsonStr(pushedMsgIdList), removeCount);
    }


    public void addToPushQueue(ChatMsg chatMsg) {
        redisService.addZSet(PUSH_GROUP_MSG_QUEUE, chatMsg, Long.valueOf(chatMsg.getCreatedTime().getTime()).doubleValue(), 10, TimeUnit.MINUTES);
    }
}