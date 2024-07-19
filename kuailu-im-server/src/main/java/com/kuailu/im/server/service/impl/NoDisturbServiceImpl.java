package com.kuailu.im.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kuailu.im.server.constant.RedisCacheKey;
import com.kuailu.im.server.mapper.NoDisturbMapper;
import com.kuailu.im.server.model.entity.NoDisturb;
import com.kuailu.im.server.model.entity.UserAccount;
import com.kuailu.im.server.service.INoDisturbService;
import com.kuailu.im.server.util.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 免打扰表 服务实现类
 * </p>
 *
 * @author liangdl
 * @since 2023-05-17
 */
@Service
@Slf4j
public class NoDisturbServiceImpl extends ServiceImpl<NoDisturbMapper, NoDisturb> implements INoDisturbService {
    @Autowired
    RedisService redisService;


    @Override
    public NoDisturb getOne(String userId, String conversationId) {
        NoDisturb noDisturb = getOne(new LambdaQueryWrapper<NoDisturb>().eq(NoDisturb::getUserId, userId).eq(NoDisturb::getConversationId, conversationId));
        return noDisturb;
    }

    public List<String> getNoDisturbUserId(String conversationId) {
        List<String> noDisturbUserIdList = new ArrayList<>();
        List<NoDisturb> noDisturbList = list(new QueryWrapper<NoDisturb>().select("user_id").lambda().eq(NoDisturb::getConversationId, conversationId));
        if (CollectionUtils.isNotEmpty(noDisturbList)) {
            noDisturbUserIdList = noDisturbList.stream().map(NoDisturb::getUserId).collect(Collectors.toList());
        }

        return noDisturbUserIdList;
    }


    public List<String> getNoDisturbConversationIdList(String userId) {
        List<String> noDisturbConversationIdList = new ArrayList<>();
        List<NoDisturb> noDisturbList = list(new QueryWrapper<NoDisturb>().select("conversation_id").lambda().eq(NoDisturb::getUserId, userId));
        if (CollectionUtils.isNotEmpty(noDisturbList)) {
            noDisturbConversationIdList = noDisturbList.stream().map(NoDisturb::getConversationId).collect(Collectors.toList());
        }

        return noDisturbConversationIdList;
    }

    public List<NoDisturb> getNoDisturbConversationIdList(String userId,List<String> conversationIdList) {
        List<NoDisturb> noDisturbList = list(new QueryWrapper<NoDisturb>()
                .select("conversation_id","user_id").lambda()
                .in(NoDisturb::getConversationId,conversationIdList)
                .eq(NoDisturb::getUserId, userId));
        return noDisturbList;
    }


}
