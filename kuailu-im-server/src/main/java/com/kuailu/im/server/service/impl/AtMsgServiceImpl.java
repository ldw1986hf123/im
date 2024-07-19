package com.kuailu.im.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.annotations.VisibleForTesting;
import com.kuailu.im.server.constant.RedisCacheKey;
import com.kuailu.im.server.enums.YesOrNoEnum;
import com.kuailu.im.server.mapper.AtMsgMapper;
import com.kuailu.im.server.model.entity.AtMsg;
import com.kuailu.im.server.model.entity.ChatGroupMember;
import com.kuailu.im.server.model.entity.ChatMsg;
import com.kuailu.im.server.req.MessageBody;
import com.kuailu.im.server.response.MessageHistoryResponse;
import com.kuailu.im.server.service.IAtMsgService;
import com.kuailu.im.server.service.IChatGroupMemberService;
import com.kuailu.im.server.service.IChatMsgService;
import com.kuailu.im.server.util.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AtMsgServiceImpl extends ServiceImpl<AtMsgMapper, AtMsg> implements IAtMsgService {

    private final String ALL_AT_USER = "AtAllUserTag";


    private final String AT_MSG_CACHE = "AT_MSG_CACHE" + RedisCacheKey.SUFFIX;

    @Autowired
    IChatGroupMemberService groupMemberService;

    @Autowired
    RedisService redisService;

    @Autowired(required = false)
    @Lazy
    IChatMsgService chatMsgService;

    @Override
    public void saveAtMsg(MessageBody messageBody, String messageId, String sender, String groupId, String conversationId) {
        List<AtMsg> atMsgList = new ArrayList<>();
        List<String> atUserList = getAtUserId(messageBody, groupId);
        List<String> cacheKeys = new ArrayList<>();

        for (String atUser : atUserList) {
            AtMsg atMsg = new AtMsg(messageId, atUser, groupId, conversationId);
            atMsg.setCreatedBy(sender);
            //todo 创建时间应该由数据库生成
            atMsg.setCreatedTime(new Date());
            atMsg.setIsRead(sender.equals(atUser) ? YesOrNoEnum.YES.getCode() : YesOrNoEnum.NO.getCode());
            atMsgList.add(atMsg);
            cacheKeys.add(getKey(atUser, groupId));
        }
        saveBatch(atMsgList);
        redisService.delKeys(cacheKeys);
    }


    @Override
    public List<String> getAllReadAtUserByMessage(MessageBody messageBody, String messageId, String groupId) {
        List<String> allReadUserIdList = new ArrayList<>();
        List<String> atUserIdList = messageBody.getAtUserList();
        if (CollectionUtils.isNotEmpty(atUserIdList)) {
            for (String atUserId : atUserIdList) {
                List<AtMsg> atMsgList = getAtMsg(atUserId, groupId);
                if (atMsgList.stream().anyMatch(atMsg -> (YesOrNoEnum.YES.getCode() == atMsg.getIsRead() && messageId.equals(atMsg.getMsgId())))) {
                    allReadUserIdList.add(atUserId);
                }
            }
        }
        return allReadUserIdList;
    }

    @Override
    public void readAll(String userId, String groupId) {
        LambdaUpdateWrapper updateWrapper = new UpdateWrapper<AtMsg>()
                .set("is_read", YesOrNoEnum.YES.getCode()).lambda()
//                .in(AtMsg::getMsgId, messageIdList)
                .eq(AtMsg::getGroupId, groupId)
                .eq(AtMsg::getIsRead, YesOrNoEnum.NO.getCode())
                .eq(AtMsg::getAtUser, userId);
        update(updateWrapper);
        //更新缓冲中的数据
        redisService.delKey(getKey(userId, groupId));
    }

    @Override
    public MessageHistoryResponse.HistoryChat getLastUnReadMsg(String groupId, String userId) {
        MessageHistoryResponse.HistoryChat historyChat = new MessageHistoryResponse().getHistoryInstance(); 
        LambdaQueryWrapper queryWrapper = new QueryWrapper<AtMsg>()
                .last("limit " + 1).orderByDesc("created_time")
                .select("msg_id")
                .lambda().eq(AtMsg::getGroupId, groupId)
                .eq(AtMsg::getIsRead, YesOrNoEnum.NO.getCode())
                .eq(AtMsg::getAtUser, userId);
        AtMsg lastUnReadMsg = getOne(queryWrapper);
        if (null != lastUnReadMsg) {
            historyChat = BeanUtil.copyProperties(chatMsgService.getByMessageId(groupId, lastUnReadMsg.getMsgId()), MessageHistoryResponse.HistoryChat.class);
            historyChat.setId(lastUnReadMsg.getMsgId());
        }

        return historyChat;
    }

    /**
     * 按照时间倒序返回
     *
     * @param userId
     * @param groupId
     * @return
     */
    private List<AtMsg> getAtMsg(String userId, String groupId) {
        String key = getKey(userId, groupId);
        List<AtMsg> atMsgList = redisService.reverseRange(key, AtMsg.class);
        if (CollectionUtils.isEmpty(atMsgList)) {
            LambdaQueryWrapper lambdaQueryWrapper = new QueryWrapper<AtMsg>().lambda()
                    .eq(AtMsg::getAtUser, userId)
                    .eq(AtMsg::getGroupId, groupId);
            atMsgList = list(lambdaQueryWrapper);

            for (AtMsg atMsg : atMsgList) {
                if (null == atMsg.getCreatedTime()) {
                    atMsg.setCreatedTime(new Date());
                }
                redisService.addZSet(key, atMsg, Double.valueOf(atMsg.getCreatedTime().getTime()), 10, TimeUnit.MINUTES);
            }
        }
        return atMsgList;
    }


    private String getKey(String userId, String groupId) {
        return "AT_MSG_CACHE" + RedisCacheKey.SUFFIX + groupId + RedisCacheKey.SUFFIX + userId;
    }


    @Override
    public List<String> extractAtUserIdInMag(ChatMsg chatMsg, String groupId) {
        MessageBody messageBody = JSONUtil.toBean(chatMsg.getMsg(), MessageBody.class);
        return getAtUserId(messageBody, groupId);
    }

    private List<String> getAtUserId(MessageBody messageBody, String groupId) {
        List<String> atUserList = messageBody.getAtUserList();
        if (ALL_AT_USER.equals(atUserList.get(0))) {
            List<ChatGroupMember> groupMemberList = groupMemberService.getAllMembers(groupId);
            atUserList = groupMemberList.stream().map(ChatGroupMember::getUserId).collect(Collectors.toList());
        }
        return atUserList;
    }
}
