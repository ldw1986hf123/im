package com.kuailu.im.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kuailu.im.core.packets.ChatType;
import com.kuailu.im.core.packets.UserDto;
import com.kuailu.im.server.constant.RedisCacheKey;
import com.kuailu.im.server.dto.UserCacheDto;
import com.kuailu.im.server.enums.MessageOperaTypeEnum;
import com.kuailu.im.server.enums.MessageTypeEnum;
import com.kuailu.im.server.mapper.MergedMsgMapper;
import com.kuailu.im.server.model.ResponseModel;
import com.kuailu.im.server.model.entity.ChatMsg;
import com.kuailu.im.server.model.entity.MergedMsg;
import com.kuailu.im.server.model.entity.UserAccount;
import com.kuailu.im.server.req.ChatReqParam;
import com.kuailu.im.server.req.MessageBody;
import com.kuailu.im.server.response.MergeEntityDetailResponse;
import com.kuailu.im.server.response.MessageHistoryResponse;
import com.kuailu.im.server.service.IChatMsgService;
import com.kuailu.im.server.service.IMergedMsgService;
import com.kuailu.im.server.service.IUserAccountService;
import com.kuailu.im.server.util.ApplicationContextHelper;
import com.kuailu.im.server.util.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sound.midi.Receiver;
import java.util.*;


@Service
@Slf4j
public class MergedMsgServiceImpl extends ServiceImpl<MergedMsgMapper, MergedMsg> implements IMergedMsgService {

    @Autowired
    RedisService redisService;

    @Override
    public void saveMergeMessage(List<String> mergedMessageIdList, String messageId, Integer msgType, String senderName, String receiver, String mergedUserName, String currentUserId) {
        String firstMsgId = mergedMessageIdList.get(0);
        IChatMsgService chatMsgService = ApplicationContextHelper.get().getBean(IChatMsgService.class);
        ChatMsg chatMsg = chatMsgService.getOne(new QueryWrapper<ChatMsg>().lambda().eq(ChatMsg::getMessageId, firstMsgId));

        MergedMsg mergedMsg = new MergedMsg();
        mergedMsg.setMessageId(messageId);
        mergedMsg.setMergedMessageId(StringUtils.join(mergedMessageIdList, ","));
        mergedMsg.setChatType(chatMsg.getChatType());
        mergedMsg.setMsgType(msgType);
        mergedMsg.setCreatedTime(new Date());

        String title = "";
        if (ChatType.CHAT_TYPE_PRIVATE.getNumber() == chatMsg.getChatType()) {
            //如果合并的是自己的会话
            if (chatMsg.getReceiver().equals(currentUserId)) {
                title = senderName + "的会话记录";
            } else {
                title = senderName + "和" + mergedUserName + "的会话记录";
            }
        } else if (ChatType.FILE_HELPER.getNumber() == chatMsg.getChatType()) {
            title = senderName + "的会话记录";
        } else {
            title = "群聊会话记录";
        }
        mergedMsg.setTitle(title);
        mergedMsg.setId(IdUtil.fastUUID());
        save(mergedMsg);
    }

    @Override
    public void saveMergeMessage(String mergedMessageId, String messageId, Integer msgType, String senderName, String receiver, String mergedUserName) {
        MergedMsg mergedMsg = new MergedMsg();
        mergedMsg.setMessageId(messageId);

        MergedMsg mergedEntity = getByMessageId(mergedMessageId);
        if (null == mergedEntity) {
            log.error("数据有误，mergedMessageId  {} 找不到对应的实体", mergedMessageId);
            return;
        }
        IChatMsgService chatMsgService = ApplicationContextHelper.get().getBean(IChatMsgService.class);
        ChatMsg chatMsg = chatMsgService.getOne(new QueryWrapper<ChatMsg>().lambda().eq(ChatMsg::getMessageId, mergedMessageId));
        mergedMsg.setEntityId(mergedEntity.getId());
        mergedMsg.setTitle(mergedEntity.getTitle());
        mergedMsg.setChatType(chatMsg.getChatType());
        mergedMsg.setMsgType(msgType);
        mergedMsg.setCreatedTime(new Date());
        save(mergedMsg);
    }


    @Override
    public MessageBody formMergePushMessage(List<String> mergedMessageIdList, Integer chatType, String mergedUserName, String currentUserId) {
        MessageBody messageBody = new MessageBody();
        IChatMsgService chatMsgService = ApplicationContextHelper.get().getBean(IChatMsgService.class);
        List<ChatMsg> chatMsgs = chatMsgService.list(new QueryWrapper<ChatMsg>().orderByAsc("created_time").last("limit 4").lambda().in(ChatMsg::getMessageId, mergedMessageIdList));

        List<Map<String, Object>> mergeMessageList = new ArrayList();
        for (ChatMsg chatMsg : chatMsgs) {
            Integer msgType = chatMsg.getMsgType();
            Map<String, Object> mergeMessageMap = new HashMap<>();
            if (MessageTypeEnum.MERGE_REDIRECT.getCode() == msgType) {
                MergedMsg mergedMsgMessageBody = getOne(new QueryWrapper<MergedMsg>().lambda().eq(MergedMsg::getMessageId, chatMsg.getMessageId()));
                mergeMessageMap.put("mergeEntityId", mergedMsgMessageBody.getId());
                mergeMessageMap.put("mergedTitle", mergedMsgMessageBody.getTitle());
            }
            mergeMessageMap.put("content", JSONUtil.parseObj(chatMsg.getMsg()).get("content"));
            mergeMessageMap.put("senderName", chatMsg.getSenderName());
            mergeMessageMap.put("msgType", chatMsg.getMsgType());
            mergeMessageMap.put("messageId", chatMsg.getMessageId());
            mergeMessageList.add(mergeMessageMap);
        }

        String title = "";
        if (ChatType.CHAT_TYPE_PRIVATE.getNumber() == chatType) {
            UserCacheDto userCacheDto = redisService.getHashValue(RedisCacheKey.ONLINE_USER_CACHE + currentUserId, UserCacheDto.class);
            title = userCacheDto.getUserName() + "和" + mergedUserName + "的会话";
        } else {
            title = "群聊";
        }
        messageBody.setMergedTitle(title);
        messageBody.setMergeMessageList(mergeMessageList);
        return messageBody;
    }

    @Override
    public MessageBody formMergePushMessage(String mergedMessageId, String mergedUserName) {
        MessageBody messageBody = new MessageBody();

        MergedMsg mergedMsg = getByMessageId(mergedMessageId);
        if (null == mergedMsg) {
            log.error("数据有误，mergedMessageId  {} 找不到对应的实体", mergedMessageId);
            return messageBody;
        }
        List<String> mergedMessageIdList = new ArrayList<>();
        MergedMsg mergedEntitySelf = getByMessageId(mergedMessageId);
        String mergeMessageIdStr = mergedEntitySelf.getMergedMessageId();
        if (StringUtils.isEmpty(mergeMessageIdStr)) {
            MergedMsg buttonEntity = getButtonLevelMergeEntity(mergedMsg.getEntityId());
            mergedMessageIdList = Arrays.asList(buttonEntity.getMergedMessageId().split(","));
        } else {
            mergedMessageIdList = Arrays.asList(mergeMessageIdStr.split(","));
        }


        IChatMsgService chatMsgService = ApplicationContextHelper.get().getBean(IChatMsgService.class);
        List<ChatMsg> chatMsgs = chatMsgService.list(new QueryWrapper<ChatMsg>().orderByAsc("created_time").last("limit 4").lambda().in(ChatMsg::getMessageId, mergedMessageIdList));

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
        messageBody.setMergedTitle(mergedEntitySelf.getTitle());
        messageBody.setMergeEntityId(mergedEntitySelf.getId());
        messageBody.setMergeMessageList(mergeMessageList);
        return messageBody;
    }


    @Override
    public MergedMsg getByMessageId(String messageId) {
        MergedMsg mergedEntity = getOne(new QueryWrapper<MergedMsg>().lambda().eq(MergedMsg::getMessageId, messageId));
        return mergedEntity;
    }

    @Override
    public ResponseModel getMergeEntityDetail(String messageId) {
        ResponseModel responseModel = ResponseModel.error();
        IChatMsgService chatMsgService = ApplicationContextHelper.get().getBean(IChatMsgService.class);
        ChatMsg chatMsg = chatMsgService.getOne(new QueryWrapper<ChatMsg>().lambda().eq(ChatMsg::getMessageId, messageId));

        String operaType = chatMsg.getOperaType();

        List<ChatMsg> chatMsgs = new ArrayList<>();
        MergedMsg mergedMsg = getOne(new QueryWrapper<MergedMsg>().lambda().eq(MergedMsg::getMessageId, messageId));
        List<String> mergedMessageIdList = new ArrayList<>();
        if (MessageOperaTypeEnum.RedirectMerge.getCode().equals(operaType)) {
            MergedMsg mergedEntitySelf = getById(mergedMsg.getEntityId());
            mergedMessageIdList = Arrays.asList(mergedEntitySelf.getMergedMessageId().split(","));
        } else if (MessageOperaTypeEnum.MergeRedirect.getCode().equals(operaType)) {
            recursionSearchMergeMessage(messageId, mergedMessageIdList);

        }
        chatMsgs = chatMsgService.list(new QueryWrapper<ChatMsg>().orderByAsc("created_time").lambda().in(ChatMsg::getMessageId, mergedMessageIdList));
        List<MergeEntityDetailResponse> resultList = new ArrayList<>();
        for (ChatMsg singleChaMsg : chatMsgs) {
            String nextLevelMessageId = singleChaMsg.getMessageId();
            MergeEntityDetailResponse mergeEntityDetailResponse = BeanUtil.copyProperties(singleChaMsg, MergeEntityDetailResponse.class);
            mergeEntityDetailResponse.setId(nextLevelMessageId);
            String msg = singleChaMsg.getMsg();
            if (StringUtils.isEmpty(msg)) {
                continue;
            }
            if (MessageTypeEnum.MERGE_REDIRECT.getCode() == singleChaMsg.getMsgType()) {
                MergedMsg nextLevelMergedMsg = getByMessageId(nextLevelMessageId);
                List<String> nextLevelMessageIdList = Arrays.asList(nextLevelMergedMsg.getMergedMessageId().split(","));
                List<ChatMsg> nextLevelChatMsgs = chatMsgService.list(new QueryWrapper<ChatMsg>().orderByAsc("created_time").last("limit 4").lambda().in(ChatMsg::getMessageId, nextLevelMessageIdList));

                List<Map<String, Object>> mergeMessageList = new ArrayList();
                MessageBody messageBody = new MessageBody();

                for (ChatMsg nextLevelChatMsg : nextLevelChatMsgs) {
                    Map<String, Object> mergeMessageMap = new HashMap<>();
                    String nextLevelMsg = nextLevelChatMsg.getMsg();
                    if (StringUtils.isNotEmpty(nextLevelMsg)) {
                        mergeMessageMap.put("content", JSONUtil.parseObj(nextLevelMsg).get("content"));
                        mergeMessageMap.put("senderName", nextLevelChatMsg.getSenderName());
                    }
                    mergeMessageMap.put("msgType", nextLevelChatMsg.getMsgType());
                    mergeMessageMap.put("messageId", nextLevelChatMsg.getMessageId());
                    mergeMessageList.add(mergeMessageMap);
                }
                messageBody.setMergeMessageList(mergeMessageList);
                messageBody.setMergedTitle(nextLevelMergedMsg.getTitle());
                messageBody.setMergeLevel(nextLevelMergedMsg.getLevel());
                messageBody.setMergeEntityId(nextLevelMergedMsg.getId());
                messageBody.setChatType(nextLevelMergedMsg.getChatType());
                mergeEntityDetailResponse.setMessageBody(messageBody);
            } else {
                mergeEntityDetailResponse.setMessageBody(JSONUtil.toBean(msg, MessageBody.class));
            }
            resultList.add(mergeEntityDetailResponse);
        }

        responseModel = ResponseModel.success(resultList);
        return responseModel;
    }


    @Override
    public List<String> recursionSearchMergeMessage(String messageId, List<String> mergedMessageIdList) {
        IChatMsgService chatMsgService = ApplicationContextHelper.get().getBean(IChatMsgService.class);
        ChatMsg chatMsg = chatMsgService.getOne(new QueryWrapper<ChatMsg>().lambda().eq(ChatMsg::getMessageId, messageId));

        MergedMsg mergedMsg = null;
        if (MessageTypeEnum.MERGE_REDIRECT.getCode() == chatMsg.getMsgType()) {
            //仍然要往下一层找到被合并的消息
            mergedMsg = getByMessageId(chatMsg.getMessageId());
            List<String> messageIdList = Arrays.asList(mergedMsg.getMergedMessageId().split(","));
            for (String nextLevelMessageId : messageIdList) {
                ChatMsg nextLevelChatMsg = chatMsgService.getOne(new QueryWrapper<ChatMsg>().lambda().eq(ChatMsg::getMessageId, nextLevelMessageId));
                MergedMsg nextLevelMergedMsg = getByMessageId(nextLevelChatMsg.getMessageId());
                if (null != nextLevelMergedMsg && StringUtils.isNotEmpty(nextLevelMergedMsg.getEntityId())) {
//                    MergedMsg nextLevelMergedMsg = getByMessageId(nextLevelChatMsg.getMessageId());
                    MergedMsg nextLevelEntity = getById(nextLevelMergedMsg.getEntityId());
                    mergedMessageIdList.addAll(Arrays.asList(nextLevelEntity.getMergedMessageId().split(",")));
                } else {
                    mergedMessageIdList.add(nextLevelMessageId);
                }
            }

        } else {
            mergedMessageIdList.add(chatMsg.getMessageId());
        }
        return mergedMessageIdList;
    }


    @Override
    public MergedMsg getButtonLevelMergeEntity(String entityId) {
        MergedMsg buttonMergedMsg = getById(entityId);
        if (StringUtils.isNotEmpty(buttonMergedMsg.getEntityId())) {
            buttonMergedMsg = getButtonLevelMergeEntity(buttonMergedMsg.getEntityId());
        }

        return buttonMergedMsg;
    }

}
