package com.kuailu.im.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kuailu.im.server.constant.RedisCacheKey;
import com.kuailu.im.server.dto.AtMsgCacheDto;
import com.kuailu.im.server.model.entity.AtMsg;
import com.kuailu.im.server.model.entity.ChatMsg;
import com.kuailu.im.server.response.MessageHistoryResponse;
import com.kuailu.im.server.service.IAtMsgService;
import com.kuailu.im.server.service.IChatMsgService;
import com.kuailu.im.server.starter.BaseJunitTest;
import com.kuailu.im.server.util.RedisService;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class AtMsgServiceImplTest extends BaseJunitTest {

    @Autowired
    IAtMsgService atMsgService;

    @Autowired
    RedisService redisService;

    @Autowired
    IChatMsgService chatMsgService;

    @Autowired
    RedisTemplate redisTemplate;

    @Test
    void saveAtMsg() {
    }

    @Test
    void getReadUserId() {
    }

    @Test
    void getAllAtUserByMessageId() {
    }

    private String getKey(String userId, String groupId) {
        return "AT_MSG_CACHE" + RedisCacheKey.SUFFIX + groupId + RedisCacheKey.SUFFIX + userId;
    }

    @Test
    void read() {
        String key = getKey("d9e46d24-dbe7-4d86-88b2-7cf8afa935a1", "c6cb52b45ab44d2a9562f2bd8db99b35");
        List<AtMsg> atMsgList = redisService.range(key, AtMsg.class);
        if (CollectionUtils.isEmpty(atMsgList)) {
            LambdaQueryWrapper lambdaQueryWrapper = new QueryWrapper<AtMsg>().lambda()
                    .eq(AtMsg::getAtUser, "d9e46d24-dbe7-4d86-88b2-7cf8afa935a1")
                    .eq(AtMsg::getGroupId, "c6cb52b45ab44d2a9562f2bd8db99b35");
            atMsgList = atMsgService.list(lambdaQueryWrapper);
            redisService.addZSet(key, atMsgList);
        }
    }

    @Test
    void testRead() {
        redisTemplate.opsForHash().put("atMsg", "11", "@dasa");
        redisTemplate.opsForHash().put("atMsg", "22", "@12112");
    }

    @Test
    void getLastUnReadMsg() {
        String userId = "b354a2db-3bf0-4946-9965-04a152ef3fe7";
        String groupId = "b86e4dd6a3784e5f8e59075fe523cb20";
        String key = getKey(userId, groupId);

        List<AtMsg> atMsgList = redisService.range(key, AtMsg.class);
        LambdaQueryWrapper lambdaQueryWrapper = new QueryWrapper<AtMsg>().lambda()
                .eq(AtMsg::getAtUser, userId)
                .eq(AtMsg::getGroupId, groupId);
        atMsgList = atMsgService.list(lambdaQueryWrapper);

        for (AtMsg atMsg : atMsgList) {
//            redisService.addZSet(key, atMsg, Double.valueOf(atMsg.getCreatedTime().getTime()), 10, TimeUnit.MINUTES);
            redisTemplate.opsForZSet().add(key, atMsg, 0);

        }


//        Set<AtMsg> atMsgs = redisTemplate.opsForZSet().range(key, 0, -1);

        AtMsg date = atMsgList.get(0);
//        AtMsgCacheDto atMsgCacheDto=BeanUtil.copyProperties(date, AtMsgCacheDto.class);
        redisTemplate.opsForZSet().add("11", date, Double.valueOf(date.getCreatedTime().getTime()));

        Set<AtMsg> date1 =   redisTemplate.opsForZSet().range("11", 0, -1);
        printResult(date1);
    }
}