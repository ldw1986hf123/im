package com.kuailu.im.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.kuailu.im.core.ImPacket;
import com.kuailu.im.core.ImStatus;
import com.kuailu.im.core.common.PublicRedisKey;
import com.kuailu.im.core.exception.AppException;
import com.kuailu.im.core.packets.ChatType;
import com.kuailu.im.core.packets.Command;
import com.kuailu.im.core.packets.RespBody;
import com.kuailu.im.core.param.ApassChatReqParam;
import com.kuailu.im.server.JimServerAPI;
import com.kuailu.im.server.apass.controller.ApassConstant;
import com.kuailu.im.server.enums.MessageOperaTypeEnum;
import com.kuailu.im.server.enums.MessageStatusEnum;
import com.kuailu.im.server.enums.MessageTypeEnum;
import com.kuailu.im.server.enums.YesOrNoEnum;
import com.kuailu.im.server.mapper.ChatMsgMapper;
import com.kuailu.im.server.model.ResponseModel;
import com.kuailu.im.server.model.entity.*;
import com.kuailu.im.server.processor.param.RevokeMessageParam;
import com.kuailu.im.server.req.ChatReqParam;
import com.kuailu.im.server.req.MessageBody;
import com.kuailu.im.server.response.MessageHistoryResponse;
import com.kuailu.im.server.response.RevokeMessageResponse;
import com.kuailu.im.server.service.*;
import com.kuailu.im.server.util.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 */
@Service
@Slf4j
public class ChatMsgServiceImpl extends ServiceImpl<ChatMsgMapper, ChatMsg> implements IChatMsgService {
    final String CHAT_MESSAGE_CONTENT_CACHE = "CHAT_MESSAGE_CONTENT_CACHE_";


    @Autowired
    IChatGroupService groupService;
    @Autowired
    KafkaService kafkaService;
    @Autowired
    IMergedMsgService mergedMsgService;

    @Autowired
    IChatUnreadMsgService unreadMsgService;

    @Autowired
    RedisService redisService;

    @Autowired
    IConversationService conversationService;


    @Autowired(required = false)
    @Lazy
    INoDisturbService noDisturbService;


    @Autowired
    IChatGroupMemberService groupMemberService;


    @Autowired
    IAtMsgService atMsgService;

    @Autowired
    IUserAccountService userAccountService;

    @Value("${kuailu.apiUrl}")
    String kuailuApiUrl;

    @Value("${kuailu.userPortalAppId}")
    String userPortalAppId;

  /*  public static Cache<String, List<String>> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)// 注意和expireAfterAccess的区别
            .maximumSize(100) // 缓存最大容量，超过时会根据缓存策略进行清理
            .build();*/


    @Override
    public List<ChatMsg> getMessageHistory(Integer count, String groupId, Long endTime) {
        List<ChatMsg> chatMsgHistoryList = new ArrayList();
        List<String> idList = Lists.newArrayList();

        QueryWrapper<ChatMsg> queryWrapper = new QueryWrapper();
        queryWrapper.select("message_id")
//                .last("limit " + count)
                .orderByDesc("created_time").lambda()
                .eq(ChatMsg::getGroupId, groupId)
                .lt(ChatMsg::getCreatedTime, DateUtil.date(endTime));

        if (null != count) {
            queryWrapper.last("limit " + count);
        }

        List<ChatMsg> chatMsgList = list(queryWrapper);
        if (CollectionUtils.isNotEmpty(chatMsgList)) {
            idList = chatMsgList.stream().map(ChatMsg::getMessageId).collect(Collectors.toList());
            //获取消息的具体内容
            for (String messageId : idList) {
                chatMsgHistoryList.add(getByMessageId(groupId, messageId));
            }
        }
        return chatMsgHistoryList;
    }


    @Override
    public ChatMsg getLastMsg(String groupId) {
        QueryWrapper<ChatMsg> queryWrapper = new QueryWrapper();
        queryWrapper.last("limit " + 1).orderByDesc("created_time").lambda().eq(ChatMsg::getGroupId, groupId);
        ChatMsg lastChatMsg = getOne(queryWrapper);
        return lastChatMsg;
    }

    public int getPrivateUnreadCountByGroupId(String groupId, String currentUserId) {
        int unreadCount = 0;
        try {
            LambdaQueryWrapper queryWrapper = new QueryWrapper<ChatMsg>().lambda()
                    .eq(ChatMsg::getGroupId, groupId)
                    .eq(ChatMsg::getReceiver, currentUserId)
                    .eq(ChatMsg::getIsRead, YesOrNoEnum.NO.getCode());
            unreadCount = count(queryWrapper);
        } catch (Exception e) {
            log.error("获取私聊未读数异常", e);
        }
        return unreadCount;
    }


    @Override
    public int getGroupUnreadCount(String groupId, String currentUserId) {
        int unreadCount = 0;
        ChatUnreadMsg chatUnreadMsg = unreadMsgService.getOne(new QueryWrapper<ChatUnreadMsg>().select("msg_id").lambda()
                .eq(ChatUnreadMsg::getGroupId, groupId)
                .eq(ChatUnreadMsg::getUserId, currentUserId));
        ChatMsg lastMessage = null;
        if (null != chatUnreadMsg) {
            String messageId = chatUnreadMsg.getMsgId();
            lastMessage = getOne(new QueryWrapper<ChatMsg>().select("created_time").lambda().eq(ChatMsg::getMessageId, messageId));
            //自己发的，不算未读
           /* LambdaQueryWrapper lambdaQueryWrapper = new QueryWrapper<ChatMsg>().lambda()
                    .eq(ChatMsg::getReceiver, groupId)
                    .ne(ChatMsg::getSender, currentUserId)
                    .gt(ChatMsg::getCreatedTime, lastMessage.getCreatedTime());
            unreadCount = count(lambdaQueryWrapper);*/
            LambdaQueryWrapper lambdaQueryWrapper = new QueryWrapper<ChatMsg>().lambda()
                    .eq(ChatMsg::getReceiver, groupId)
                    .ne(ChatMsg::getSender, currentUserId)
                    .gt(ChatMsg::getCreatedTime, lastMessage.getCreatedTime());
            unreadCount = count(lambdaQueryWrapper);
        } else {
            LambdaQueryWrapper lambdaQueryWrapper = new QueryWrapper<ChatMsg>().lambda()
                    .eq(ChatMsg::getReceiver, groupId)
                    .ne(ChatMsg::getSender, currentUserId);
//                    .gt(ChatMsg::getCreatedTime, lastMessage.getCreatedTime());
            unreadCount = count(lambdaQueryWrapper);
        }
        return unreadCount;
    }

    @Override
    public void confirmReadMsg(String currentUserId, String groupId, String messageId) {
        ChatGroup chatGroup = groupService.getOne(new QueryWrapper<ChatGroup>().select("chat_type").lambda().eq(ChatGroup::getGroupId, groupId));
        ChatMsg lastReadMsg = getOne(new QueryWrapper<ChatMsg>().select("created_time").lambda().eq(ChatMsg::getMessageId, messageId));

        //todo 应该优化掉
        if (ChatType.CHAT_TYPE_PRIVATE.getNumber() == chatGroup.getChatType() || ChatType.FILE_HELPER.getNumber() == chatGroup.getChatType()) {
            LambdaQueryWrapper queryWrapper = new QueryWrapper<ChatMsg>()
                    .select("message_id")
                    .lambda()
                    .eq(ChatMsg::getIsRead, YesOrNoEnum.NO.getCode())
                    .eq(ChatMsg::getReceiver, currentUserId)
                    .le(ChatMsg::getCreatedTime, lastReadMsg.getCreatedTime());
            List<ChatMsg> updateMessageList = list(queryWrapper);
            List<String> updateMessageIdList = updateMessageList.stream().map(ChatMsg::getMessageId).collect(Collectors.toList());
            cleanMsgContent(groupId, updateMessageIdList);

            LambdaUpdateWrapper updateWrapper = new UpdateWrapper<ChatMsg>()
                    .set("is_read", YesOrNoEnum.YES.getCode())
                    .lambda()
                    .eq(ChatMsg::getIsRead, YesOrNoEnum.NO.getCode())
                    .eq(ChatMsg::getReceiver, currentUserId)
                    .le(ChatMsg::getCreatedTime, lastReadMsg.getCreatedTime());
            update(updateWrapper);

        } else {
            ChatUnreadMsg chatUnreadMsg = new ChatUnreadMsg();
            chatUnreadMsg.setMsgId(messageId)
                    .setUserId(currentUserId)
                    .setGroupId(groupId)
                    .setCreatedBy(currentUserId);

            UpdateWrapper<ChatUnreadMsg> updateWrapper = new UpdateWrapper<ChatUnreadMsg>()
                    .eq("user_id", currentUserId)
                    .eq("group_id", groupId);
            unreadMsgService.saveOrUpdate(chatUnreadMsg, updateWrapper);
            atMsgService.readAll(currentUserId, groupId);
        }

    }

    @Override
    public ResponseModel revoke(RevokeMessageParam revokeMessageParam) {
        ResponseModel responseModel = ResponseModel.error();
        String messageId = revokeMessageParam.getMessageId();
        ChatMsg chatMsg = getOne(new QueryWrapper<ChatMsg>().lambda().eq(ChatMsg::getMessageId, messageId));

        chatMsg.setStatus(MessageStatusEnum.REVOKED.getCode());
        chatMsg.setUpdatedTime(new Date());
        LambdaUpdateWrapper updateWrapper = new UpdateWrapper<ChatMsg>().lambda().eq(ChatMsg::getMessageId, messageId);
        update(chatMsg, updateWrapper);

        RevokeMessageResponse revokeMessageResponse = new RevokeMessageResponse(chatMsg.getMessageId(), MessageStatusEnum.REVOKED);

        ImPacket revokePacket = new ImPacket(new RespBody(Command.COMMAND_PUSH_REVOKE_MESSAGE, revokeMessageResponse).toByte());


        cleanMsgContent(chatMsg.getGroupId(), chatMsg.getMessageId());
        if (ChatType.CHAT_TYPE_PRIVATE.getNumber() == chatMsg.getChatType() || ChatType.CHAT_TYPE_MSG_HELPER.getNumber() == chatMsg.getChatType()) {
            conversationService.updatePrivateLastMsgCache(chatMsg.getConversationId(), chatMsg.getSender(), chatMsg.getReceiver());
        } else {
            conversationService.updatePublicConversationMsgCache(chatMsg.getConversationId(), chatMsg.getGroupId());
            redisService.decrement(PublicRedisKey.IM_UNREAD_MSG_COUNT + chatMsg.getReceiver());
        }

        /***************************  kafka 发推送****************************/
        String groupId = chatMsg.getGroupId();
        String conversationId = chatMsg.getConversationId();
        List<ChatGroupMember> groupMemberList = groupMemberService.getAllMembers(groupId);
        List<String> receiverUserIdList = groupMemberList.stream().map(ChatGroupMember::getUserId).collect(Collectors.toList());

        List<String> noDisturbUserIdList = noDisturbService.getNoDisturbUserId(conversationId);
        receiverUserIdList.removeAll(noDisturbUserIdList);
        //不要给自己发推送
        receiverUserIdList.remove(chatMsg.getSender());
        kafkaService.revoke(receiverUserIdList, chatMsg, groupId, conversationId);
        /***************************  kafka 发推送****************************/
        JimServerAPI.sendToGroup(chatMsg.getGroupId(), revokePacket);
        return responseModel.success();
    }

    public MessageHistoryResponse buildChatList(List<ChatMsg> chatMsgList, String receiver, String groupId) {
        MessageHistoryResponse messageData = new MessageHistoryResponse(receiver, groupId);
        List<MessageHistoryResponse.HistoryChat> historyChatLists = Lists.newArrayList();
        for (ChatMsg chatMsg : chatMsgList) {
            MessageHistoryResponse.HistoryChat historyChat = BeanUtil.copyProperties(chatMsg, MessageHistoryResponse.HistoryChat.class);
            String messageId = chatMsg.getMessageId();
            String operaType = chatMsg.getOperaType();
            Integer msgType = chatMsg.getMsgType();
            historyChat.setCreatedTime(chatMsg.getCreatedTime().getTime());

            Date updateTime = chatMsg.getUpdatedTime() == null ? chatMsg.getCreatedTime() : chatMsg.getUpdatedTime();
            historyChat.setUpdatedTime(updateTime.getTime());

            MessageBody messageBody = new MessageBody();
            if (MessageTypeEnum.MERGE_REDIRECT.getCode() == chatMsg.getMsgType()) {
                if (MessageOperaTypeEnum.MergeRedirect.getCode().equals(operaType)) {
                    messageBody = buildMergeRedirectMessage(messageId, groupId);
                } else if (MessageOperaTypeEnum.RedirectMerge.getCode().equals(operaType)) {
                    messageBody = buildRedirectMergeMessage(chatMsg);
                }
            } else {
                messageBody = JSONUtil.toBean(chatMsg.getMsg(), MessageBody.class);
            }

            if (MessageTypeEnum.ATMessage.getCode() == msgType) {
                List<String> readAtMsgUserIds = atMsgService.getAllReadAtUserByMessage(messageBody, messageId, groupId);
                historyChat.setReadAtMsgUserIds(readAtMsgUserIds);
            }

            historyChat.setMessageBody(messageBody);
            historyChat.setId(messageId);
            historyChat.setIsRead(chatMsg.getIsRead());
            historyChatLists.add(historyChat);
        }
        messageData.setChatMsgList(historyChatLists);
        return messageData;
    }


    @Override
    public MessageHistoryResponse buildChatList(List<ChatMsg> chatMsgList, String groupId) {
        MessageHistoryResponse messageData = new MessageHistoryResponse(groupId);
        List<MessageHistoryResponse.HistoryChat> historyChatLists = Lists.newArrayList();
        for (ChatMsg chatMsg : chatMsgList) {
            MessageHistoryResponse.HistoryChat historyChat = BeanUtil.copyProperties(chatMsg, MessageHistoryResponse.HistoryChat.class);
            String messageId = chatMsg.getMessageId();
            String operaType = chatMsg.getOperaType();
            Integer msgType = chatMsg.getMsgType();
            historyChat.setCreatedTime(chatMsg.getCreatedTime().getTime());

            Date updateTime = chatMsg.getUpdatedTime() == null ? chatMsg.getCreatedTime() : chatMsg.getUpdatedTime();
            historyChat.setUpdatedTime(updateTime.getTime());

            MessageBody messageBody = new MessageBody();
            if (MessageTypeEnum.MERGE_REDIRECT.getCode() == chatMsg.getMsgType()) {
                if (MessageOperaTypeEnum.MergeRedirect.getCode().equals(operaType)) {
                    messageBody = buildMergeRedirectMessage(messageId, groupId);
                } else if (MessageOperaTypeEnum.RedirectMerge.getCode().equals(operaType)) {
                    messageBody = buildRedirectMergeMessage(chatMsg);
                }
            } else {
                messageBody = JSONUtil.toBean(chatMsg.getMsg(), MessageBody.class);
            }

            if (MessageTypeEnum.ATMessage.getCode() == msgType) {
                List<String> readAtMsgUserIds = atMsgService.getAllReadAtUserByMessage(messageBody, messageId, groupId);
                historyChat.setReadAtMsgUserIds(readAtMsgUserIds);
            }

            historyChat.setMessageBody(messageBody);
            historyChat.setId(messageId);
            historyChat.setIsRead(chatMsg.getIsRead());
            historyChatLists.add(historyChat);
        }
        messageData.setChatMsgList(historyChatLists);
        return messageData;
    }


    @Override
    public MessageBody buildMergeRedirectMessage(String messageId, String groupId) {
        MessageBody messageBody = new MessageBody();
        try {
            MergedMsg mergedMsg = mergedMsgService.getOne(new QueryWrapper<MergedMsg>().lambda().eq(MergedMsg::getMessageId, messageId));
            if (null == mergedMsg) {
                log.error("数据有误，msgType 是4 但是mergedMsg 为空，messageId：{}", messageId);
                return messageBody;
            }
            List<String> mergeMessageIdList = Arrays.asList(mergedMsg.getMergedMessageId().split(","));
//        List<ChatMsg> chatMsgs = list(new QueryWrapper<ChatMsg>().orderByAsc("created_time").last("limit 5").lambda().in(ChatMsg::getMessageId, mergeMessageIdList));
            List<ChatMsg> chatMsgs = getByMessageIdList(groupId, mergeMessageIdList, 5);

            messageBody = buildMessageBody(chatMsgs);
            messageBody.setMergedTitle(mergedMsg.getTitle());
            messageBody.setMergeLevel(mergedMsg.getLevel());
            messageBody.setMergeEntityId(mergedMsg.getId());
            messageBody.setChatType(mergedMsg.getChatType());
        } catch (Exception e) {
            log.error("buildMergeRedirectMessage 异常,messageId :{},groupIdL{}", messageId, groupId, e);
        }
        return messageBody;
    }

    @Override
    public MessageBody buildRedirectMergeMessage(ChatMsg chatMsgParam) {
        MessageBody messageBody = new MessageBody();
        String messageId = chatMsgParam.getMessageId();
        String groupId = chatMsgParam.getGroupId();

        MergedMsg mergedEntity = mergedMsgService.getOne(new QueryWrapper<MergedMsg>().lambda().eq(MergedMsg::getMessageId, messageId));
        if (null == mergedEntity || StringUtils.isEmpty(mergedEntity.getEntityId())) {
            log.error("数据有误，msgType 是4 但是mergedMsg 为空，messageId：{}", messageId);
            return messageBody;
        }

        List<String> mergedMessageIdList = recursionSearchMergeMessage(messageId);
//        List<ChatMsg> chatMsgs = list(new QueryWrapper<ChatMsg>().orderByAsc("created_time").lambda().in(ChatMsg::getMessageId, mergedMessageIdList));
        List<ChatMsg> chatMsgs = getByMessageIdList(groupId, mergedMessageIdList);

        List<Map<String, Object>> mergeMessageList = new ArrayList();
        for (ChatMsg singleMergedChatMsg : chatMsgs) {
            Map<String, Object> mergeMessageMap = new HashMap<>();
            String msg = singleMergedChatMsg.getMsg();
            if (StringUtils.isNotEmpty(msg)) {
                mergeMessageMap.put("content", JSONUtil.parseObj(msg).get("content"));
                mergeMessageMap.put("senderName", singleMergedChatMsg.getSenderName());
            }
            mergeMessageMap.put("msgType", singleMergedChatMsg.getMsgType());
            mergeMessageMap.put("messageId", singleMergedChatMsg.getMessageId());
            mergeMessageList.add(mergeMessageMap);
        }
        messageBody = buildMessageBody(chatMsgs);
        messageBody.setMergedTitle(mergedEntity.getTitle());
        messageBody.setMergeEntityId(mergedEntity.getId());
        messageBody.setMergeMessageList(mergeMessageList);
        return messageBody;
    }


    private List<String> recursionSearchMergeMessage(String messageId) {
        List<String> mergedMessageIdList = new ArrayList<>();
        ChatMsg chatMsg = getOne(new QueryWrapper<ChatMsg>().lambda().eq(ChatMsg::getMessageId, messageId));

        MergedMsg mergedMsg = mergedMsgService.getByMessageId(chatMsg.getMessageId());
        if (StringUtils.isNotEmpty(mergedMsg.getEntityId())) {
            MergedMsg buttonEntity = mergedMsgService.getButtonLevelMergeEntity(mergedMsg.getEntityId());
            mergedMessageIdList = Arrays.asList(buttonEntity.getMergedMessageId().split(","));
        }
        return mergedMessageIdList;

    }


    public MessageBody buildMessageBody(List<ChatMsg> chatMsgList) {
        MessageBody messageBody = new MessageBody();
        List<Map<String, Object>> mergeMessageList = new ArrayList();
        for (ChatMsg chatMsg : chatMsgList) {
            Integer msgType = chatMsg.getMsgType();
            Map<String, Object> mergeMessageMap = new HashMap<>();
            if (MessageTypeEnum.MERGE_REDIRECT.getCode() == msgType) {
                MergedMsg mergedMsgMessageBody = mergedMsgService.getOne(new QueryWrapper<MergedMsg>().lambda().eq(MergedMsg::getMessageId, chatMsg.getMessageId()));
                mergeMessageMap.put("mergeEntityId", mergedMsgMessageBody.getId());
                mergeMessageMap.put("mergedTitle", mergedMsgMessageBody.getTitle());
            }

            String msg = chatMsg.getMsg();
            if (StringUtils.isNotEmpty(msg)) {
                mergeMessageMap.put("content", JSONUtil.parseObj(msg).get("content"));
                mergeMessageMap.put("senderName", chatMsg.getSenderName());
            }
            mergeMessageMap.put("msgType", chatMsg.getMsgType());
            mergeMessageMap.put("messageId", chatMsg.getMessageId());
            mergeMessageList.add(mergeMessageMap);
        }
        messageBody.setMergeMessageList(mergeMessageList);
        return messageBody;
    }


    public ChatMsg getByMessageId(String groupId, String messageId) {
        ChatMsg chatMsg = null;
        String key = CHAT_MESSAGE_CONTENT_CACHE + groupId;
        chatMsg = redisService.getHashValue(key, messageId, ChatMsg.class);
        if (null == chatMsg) {
            chatMsg = getOne(new QueryWrapper<ChatMsg>().lambda().eq(ChatMsg::getMessageId, messageId));
            redisService.putHash(key, messageId, chatMsg);
        }
        return chatMsg;

    }

    private List<ChatMsg> getByMessageIdList(String groupId, List<String> messageIdList, Integer limit) {
        List<ChatMsg> chatMsgList = new ArrayList<>();
        try {
            for (String messageId : messageIdList) {
                ChatMsg chatMsg = getByMessageId(groupId, messageId);
                chatMsgList.add(chatMsg);
            }
            if (CollectionUtils.isNotEmpty(chatMsgList)) {
                // 按照年龄升序排序
                // 使用自定义比较器进行排序
                Collections.sort(chatMsgList, new Comparator<ChatMsg>() {
                    @Override
                    public int compare(ChatMsg date1, ChatMsg date2) {
                        if (null != date1 && null != date2) {
                            return date1.getCreatedTime().compareTo(date2.getCreatedTime());
                        } else {
                            return 1;
                        }

                    }
                });
            }
            if (chatMsgList.size() >= limit) {
                return chatMsgList.subList(0, limit);
            }
        } catch (Exception e) {
            log.error("获取合并消息详情异常  messageIdList:{} groupId :{}", JSONUtil.toJsonStr(messageIdList), groupId, e);
        }
        return chatMsgList;
    }

    private List<ChatMsg> getByMessageIdList(String groupId, List<String> messageIdList) {
        List<ChatMsg> chatMsgList = new ArrayList<>();
        try {
            for (String messageId : messageIdList) {
                ChatMsg chatMsg = getByMessageId(groupId, messageId);
                chatMsgList.add(chatMsg);
            }
            if (CollectionUtils.isNotEmpty(chatMsgList)) {
                // 使用自定义比较器进行排序
                Collections.sort(chatMsgList, new Comparator<ChatMsg>() {
                    @Override
                    public int compare(ChatMsg date1, ChatMsg date2) {
                        return date1.getCreatedTime().compareTo(date2.getCreatedTime());
                    }
                });
            }
        } catch (Exception e) {
            log.error("获取合并消息详情异常  messageIdList:{} groupId :{}", JSONUtil.toJsonStr(messageIdList), groupId, e);
        }
        return chatMsgList;
    }


    @Override
    public void cleanMsgContent(String groupId, String messageId) {
        String key = CHAT_MESSAGE_CONTENT_CACHE + groupId;
        redisService.putHash(key, messageId, null);
    }

    @Override
    public void cleanMsgContent(String groupId, List<String> messageIdList) {
        String key = CHAT_MESSAGE_CONTENT_CACHE + groupId;
        for (String messageId : messageIdList) {
            redisService.putHash(key, messageId, null);
        }

    }


    @Override
    public List<ChatMsg> navigate(String messageId, String groupId, Integer count) {
        List<ChatMsg> chatMsgList = new ArrayList<>();
        ChatMsg chatMsg = getByMessageId(groupId, messageId);
        if (null == chatMsg) {
            throw new AppException(ImStatus.CANNOT_FIND_DATA.getCode(), "找不到的数据");
        }
//        navigateMsgVoList.add(BeanUtil.copyProperties(chatMsg, NavigateMsgVo.class));
        Date createdTime = chatMsg.getCreatedTime();
        List<ChatMsg> beforeMsgIdList = list(new QueryWrapper<ChatMsg>().last("limit " + count).orderByDesc("created_time").select("message_id").lambda().eq(ChatMsg::getGroupId, groupId).lt(ChatMsg::getCreatedTime, createdTime));
        List<ChatMsg> afterMsgIdList = list(new QueryWrapper<ChatMsg>().last("limit " + count).orderByAsc("created_time").select("message_id").lambda().eq(ChatMsg::getGroupId, groupId).gt(ChatMsg::getCreatedTime, createdTime));

        List<ChatMsg> beforeMsgList = getByMessageIdList(groupId, beforeMsgIdList.stream().map(ChatMsg::getMessageId).collect(Collectors.toList()));
        List<ChatMsg> afterMsgList = getByMessageIdList(groupId, afterMsgIdList.stream().map(ChatMsg::getMessageId).collect(Collectors.toList()));

        chatMsgList.addAll(beforeMsgList);
        chatMsgList.add(chatMsg);
        chatMsgList.addAll(afterMsgList);
        return chatMsgList;
    }


    @Override
    public List<ChatMsg> getMessageContext(Integer count, String groupId, String messageId, String direction) {
        List<String> idList = new ArrayList<>();
        List<ChatMsg> chatMsgHistoryList = new ArrayList<>();

 /*       String key = getApassMessageKey(groupId, messageId, direction);
        idList = cache.getIfPresent(key);
        if (null == idList) {*/
        QueryWrapper<ChatMsg> queryWrapper = new QueryWrapper();
        queryWrapper.select("message_id")
                .eq("group_id", groupId);
        if (null != count) {
            queryWrapper.last("limit " + count);
        }
        Date endTime = getByMessageId(groupId, messageId).getCreatedTime();
        if (ApassConstant.DIRECTION_EARLY.equals(direction)) {
            queryWrapper.orderByDesc("created_time");
            queryWrapper.lt("created_time", DateUtil.date(endTime));
        } else {
            queryWrapper.orderByAsc("created_time");
            queryWrapper.gt("created_time", DateUtil.date(endTime));
        }
        List<ChatMsg> chatMsgList = list(queryWrapper);
        idList = chatMsgList.stream().map(ChatMsg::getMessageId).collect(Collectors.toList());
          /*  cache.put(key, idList);
        }*/
        chatMsgHistoryList = getByMessageIdList(groupId, idList);
        return chatMsgHistoryList;
    }

    @Override
    public List<ChatMsg> getMessageContext(Integer count, String groupId) {
        List<String> idList = new ArrayList<>();
        Date now = new Date();
        List<ChatMsg> chatMsgList = getMessageHistory(count, groupId, now.getTime());


        List<ChatMsg> chatMsgListEarly = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(chatMsgList)) {
            chatMsgListEarly = getMessageContext(count, groupId, chatMsgList.get(0).getMessageId(), ApassConstant.DIRECTION_LATER);
        }
        chatMsgList.addAll(chatMsgListEarly);
        return chatMsgList;
    }


    @Override
    public List<Map<String, Object>> getPrivateChatRecords(String userId, String searchKey) {
        return baseMapper.getPrivateChatRecords(userId, searchKey);
    }

    @Override
    public List<Map<String, Object>> getPublicChatRecords(String userId, String searchKey) {
        return baseMapper.getPublicChatRecords(userId, searchKey);
    }

    @Override
    public List<Map<String, Object>> getPublicChatRecordDetail(String userId, String searchKey, String groupId) {
        List<Map<String, Object>> result = baseMapper.getPublicChatRecordDetail(userId, searchKey, groupId);
        for (Map<String, Object> map : result) {
            String msg = (String) map.get("msg");
            if (StringUtils.isNotEmpty(msg)) {
                JSONObject jsonObject = JSONObject.parseObject(msg);
                map.put("content", jsonObject.getString("content"));
                map.remove("msg");
            }
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getPrivateChatRecordDetail(String userId, String searchKey, String groupId) {
        List<Map<String, Object>> result = baseMapper.getPrivateChatRecordDetail(userId, searchKey, groupId);
        for (Map<String, Object> map : result) {
            String msg = (String) map.get("msg");
            if (StringUtils.isNotEmpty(msg)) {
                JSONObject jsonObject = JSONObject.parseObject(msg);
                map.put("content", jsonObject.getString("content"));
                map.remove("msg");
            }
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getChatRecordResps(String userId, String searchKey) {
        List<Map<String, Object>> resultMaps = new ArrayList<>();
        List<Map<String, Object>> resultMaps1 = getPrivateChatRecords(userId, searchKey);
        List<Map<String, Object>> resultMaps2 = getPublicChatRecords(userId, searchKey);
        resultMaps.addAll(resultMaps1);
        resultMaps.addAll(resultMaps2);
        if (org.springframework.util.CollectionUtils.isEmpty(resultMaps)) {
            return null;
        }
        for (Map<String, Object> map : resultMaps) {
            if (map.get("chatType").toString().equals(String.valueOf(ChatType.CHAT_TYPE_PRIVATE.getNumber()))) {
                String friend = (String) map.get("friend");
                String url = kuailuApiUrl + "dlfile?appid=" + userPortalAppId + "&store=_default&groupValue=" + "&subCatalog=" + friend;
                map.put("avatar", url);
                UserAccount userAccount = userAccountService.getByUserId(friend);
                if (userAccount != null) {
                    map.put("groupName", userAccount.getUserName());
                }
            }
            String searchCount = map.get("searchCount").toString();
            String searchDesc = searchCount + "条相关消息记录";
            map.put("searchDesc", searchDesc);
        }
        return resultMaps;
    }

    @Override
    public Boolean deleteByMessageId(String messageId, String groupId) {
        LambdaQueryWrapper deleteWrapper = new QueryWrapper<ChatMsg>()
                .lambda()
                .eq(ChatMsg::getMessageId, messageId)
                .eq(ChatMsg::getGroupId, groupId);
        return remove(deleteWrapper);
    }

    private String getApassMessageKey(String groupId, String messageId, String direction) {
        String key = "message_context_" + groupId + "_" + messageId + "_" + direction;
        return key;
    }

    @Override
    public void cleanApassMessageCache() {
//        cache.invalidateAll();
    }

    /**
     * 仅做通知之用，不经过服务端
     *
     * @param sender
     * @param receiver
     * @param groupId
     * @param conversationId
     * @param messageBody
     * @param msgType
     * @return
     */
    @Override
    public String send(String messageId, String sender, String receiver, String groupId, String conversationId, ApassChatReqParam.MessageBody messageBody, Integer msgType) {

        UserAccount userAccount = userAccountService.getByUserId(sender);
        ChatReqParam chatReqParam = new ChatReqParam.FileHelperBuilder(messageId, sender, userAccount.getUserName(), conversationId, JSONUtil.toJsonStr(messageBody), msgType, groupId).build();

        ImPacket chatPacket = new ImPacket(new RespBody(Command.COMMAND_CHAT_REQ, chatReqParam).toByte());
        boolean sendStatus = JimServerAPI.sendToUser(receiver, chatPacket);
        return chatReqParam.getId();
    }

    @Override
    public void updateUnReadMsgCount(String groupId, String currentUserId) {
        try {
            List<ChatGroupMember> groupMemberList = groupMemberService.getAllMembers(groupId);
            groupMemberList.remove(currentUserId);
            for (ChatGroupMember groupMember : groupMemberList) {
                String userId = groupMember.getUserId();
                //todo 不应该在发消息的时候查询每个会话的未读消息数，要优化掉
                String key = PublicRedisKey.IM_UNREAD_MSG_COUNT + userId;
                log.info("receiver：{}   PublicRedisKey.IM_UNREAD_MSG_COUN 的值 {}", userId, redisService.getValue(key, Integer.class));
                if (!redisService.hasKey(key) || null == redisService.getValue(key, Integer.class)) {
                    Integer unReceivedMsgNumber = conversationService.getTotalUnReadMsgCount(userId);
                    //永远不过期
                    redisService.setValue(key, unReceivedMsgNumber);
                } else {
                    Integer count = redisService.getValue(key, Integer.class);
                    count++;
                    redisService.setValue(key, count);
                }
            }
        } catch (Exception e) {
            log.error(" 更新总共未读数异常 groupId：{}", groupId, e);
        }
    }

}
