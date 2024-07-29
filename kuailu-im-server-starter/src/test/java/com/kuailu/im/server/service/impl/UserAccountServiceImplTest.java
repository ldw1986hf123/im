package com.kuailu.im.server.service.impl;

import com.kuailu.im.server.constant.RedisCacheKey;
import com.kuailu.im.server.dto.GroupCacheDto;
import com.kuailu.im.server.dto.UserCacheDto;
import com.kuailu.im.server.model.entity.UserAccount;
import com.kuailu.im.server.service.IUserAccountService;
import com.kuailu.im.server.starter.BaseJunitTest;
import com.kuailu.im.server.util.RedisService;
import com.kuailu.im.server.vo.ChatGroupMemberVo;
import com.kuailu.im.server.vo.UserInfoDetail;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserAccountServiceImplTest extends BaseJunitTest {

    @Autowired
    IUserAccountService userAccountService;

    @Autowired
    RedisTemplate redisTemplate;


    @Autowired
    RedisService redisService;

    @Test
    void getUserInfoByDeptIds() {
//        https://kuailu-dev.brightoilonline.com/kuailu/j?appid=com.kuailu.base.apps.org&method=getUsersByParentUnitId&seid=78a27417-7f3e-48a9-904f-4495d7eeded5,paramString:6380
        List<String> deptIds = Arrays.asList("6380");
    }

    @Test
    void getUserDetailByUserId() {
    }

    @Test
    void getCurrentUser() {
    }

    @Test
    void testGetCurrentUser() {
    }

    @Test
    void updateAccountStatus() {
    }

    @Test
    void updateByLogin() {
        UserInfoDetail userInfoDetail = new UserInfoDetail();
        userInfoDetail.setUserId("2");
        userInfoDetail.setUserNo("2no");
        userInfoDetail.setUserName("ldw2");
    }

    @Test
    void isOnLine() {

        String key = RedisCacheKey.ONLINE_USER_CACHE + "d9e46d24-dbe7-4d86-88b2-7cf8afa935a1";
        UserAccount loginUserAccount = new UserAccount();
        loginUserAccount.setUserId("d9e46d24-dbe7-4d86-88b2-7cf8afa935a1");

//        userAccountService.cacheUserInLogin(loginUserAccount);

     /*        UserCacheDto userCacheDto = redisService.getHashValue(key, UserCacheDto.class);
        printResult(userCacheDto);
*/
//        userCacheDto.getGroups().get(0).setPublicChatUnReadCount(100);


        printResult(redisService.getHashValue(key, UserCacheDto.class));

        GroupCacheDto newDto = new GroupCacheDto();
        newDto.setGroupName("asd");
        newDto.setPrivateChatUnReadCount(1122);

        List<GroupCacheDto> groupCacheDtoList = redisService.getHashValue(key, "groups", ArrayList.class);
        groupCacheDtoList.add(newDto);
        redisTemplate.opsForHash().put(key, "groups", groupCacheDtoList);
        printResult(redisService.getHashValue(key, UserCacheDto.class));
    }

    @Test
    void getAvatarUrl() {
    }

    }