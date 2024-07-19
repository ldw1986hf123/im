package com.kuailu.im.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONPath;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kuailu.im.core.packets.*;
import com.kuailu.im.core.utils.HttpUtil;
import com.kuailu.im.server.constant.RedisCacheKey;
import com.kuailu.im.server.dto.UserCacheDto;
import com.kuailu.im.server.mapper.UserAccountMapper;
import com.kuailu.im.server.model.ResponseModel;
import com.kuailu.im.server.model.entity.ChatGroup;
import com.kuailu.im.server.model.entity.ChatGroupMember;
import com.kuailu.im.server.model.entity.UserAccount;
import com.kuailu.im.server.service.IChatGroupService;
import com.kuailu.im.server.service.IConversationService;
import com.kuailu.im.server.service.IUserAccountService;
import com.kuailu.im.server.util.PassService;
import com.kuailu.im.server.util.RedisService;
import com.kuailu.im.server.vo.UserInfoDetail;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * <p>
 */
@Service
@Slf4j
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount> implements IUserAccountService {

    @Value("${kuailu.apiUrl}")
    String kuailuApiUrl;


    @Value("${kuailu.userPortalAppId}")
    String userPortalAppId;

    @Autowired
    PassService passService;

    @Autowired
    RedisService redisService;

    @Autowired
    IChatGroupService groupService;


    @Autowired
    IConversationService conversationService;

    private final String APASS_USER_CACHE = "APASS_USER_CACHE";

    /**
     * getUsersByParentUnitId  已经是获取所有子部门的用户数
     *
     * @param seid
     * @param deptIds
     * @return
     */
    @Override
    public List<ChatGroupMember> getUserInfoByDeptIds(String seid, List<String> deptIds) {
        String url = kuailuApiUrl + "j" + "?" + "appid=com.kuailu.base.apps.org&method=getUsersByParentUnitId&seid=" + seid;
        List<ChatGroupMember> totalChatGroupMembers = new ArrayList<>();
        for (String deptId : deptIds) {
            String strResp = HttpUtil.doPostBody(url, deptId);
            List<ChatGroupMember> groupMemberListDept = new ArrayList<>();
            ResponseModel model = JSON.parseObject(strResp, ResponseModel.class);
            JSONArray jsonArray = (JSONArray) model.getData();
            if (!jsonArray.isEmpty()) {
                jsonArray.forEach(item -> {
                    ChatGroupMember chatGroupMemberVo = new ChatGroupMember();
                    chatGroupMemberVo.setUserId((String) JSONPath.eval(item, "$.id"));
                    chatGroupMemberVo.setUserName((String) JSONPath.eval(item, "$.userName"));
                    chatGroupMemberVo.setUserNo((String) JSONPath.eval(item, "$.userNo"));
                    groupMemberListDept.add(chatGroupMemberVo);

                });
            }
            totalChatGroupMembers.addAll(groupMemberListDept);
        }

        return totalChatGroupMembers;
    }


    /**
     * 因为pass服务经常出问题，所以改成先从缓冲获取，从pass获取
     *
     * @param currentUserId
     * @return
     */
    @Override
    public UserAccount getByUserIdLogin(String seid, String currentUserId) throws Exception {
        //先从缓存获取
        String cacheKey = RedisCacheKey.ONLINE_USER_CACHE + currentUserId;
        UserAccount userAccount = redisService.getHashValue(cacheKey, UserAccount.class);
        if (null != userAccount) {
            return userAccount;
        }

        userAccount = getOne(new QueryWrapper<UserAccount>()
                .select("user_id", "user_name", "user_no", "avatar_url", "status", "staff_status")
                .lambda().eq(UserAccount::getUserId, currentUserId));
        if (null != userAccount) {
            return userAccount;
        }

        log.info("缓存数据库都没有该用户信息，调用pass服务获取seid:{},userId:{}", seid, currentUserId);
        UserInfoDetail userInfoDetail = passService.getUserDetailsByUserId(seid, currentUserId);
        if (null != userInfoDetail) {
            userAccount = BeanUtil.copyProperties(userInfoDetail, UserAccount.class, "id");
            //初始化文件传输助手  1.4.1 再加上
            conversationService.createFileHelper(currentUserId, userAccount.getUserName());
            conversationService.cleanUserConversationIdCache(currentUserId);
        }

        //再冲数据库查询，仍然是空，就抛出异常
        if (userAccount == null) {
            log.error("找不到该用户对应的信息,seid:{},userId:{}", seid, currentUserId);
            throw new Exception("找不到该用户对应的信息");
        }
        return userAccount;
    }


    @Override
    public void updateByLogin(UserAccount loginUserAccount) {
        String userId = loginUserAccount.getUserId();
        String seid = loginUserAccount.getSeid();

        UserAccount userAccount = new UserAccount();
        userAccount.setUserNo(loginUserAccount.getUserNo());
        userAccount.setUserName(loginUserAccount.getUserName());
        userAccount.setStaffStatus(loginUserAccount.getStaffStatus());
        userAccount.setClientType(loginUserAccount.getClientType());
        userAccount.setLastLoginTime(new Date());
        userAccount.setUserName(loginUserAccount.getUserName() == null ? "" : loginUserAccount.getUserName());
        userAccount.setSeid(loginUserAccount.getSeid());
        userAccount.setUserId(userId);
        String avatar = getAvatarUrl(userId, seid);
        userAccount.setAvatarUrl(avatar);

        UpdateWrapper<UserAccount> updateWrapper = new UpdateWrapper<UserAccount>()
                .eq("user_id", userId);
        saveOrUpdate(userAccount, updateWrapper);
    }

    @Override
    public void initMsgHelper(String receiver) {
        ChatGroup chatGroup = groupService.createMsgHelperGroup(receiver);
        groupService.createMsgHelperGroupMember(chatGroup);
        groupService.createMsgHelperConversation(receiver, chatGroup.getGroupId());
    }



  /*  @Override
    public void updateByLogin(String userId, String userNo, String userName, Integer staffStatus, String avatar, String seid) {
        UserAccount userAccount = new UserAccount();
        userAccount.setUserId(userId);
        userAccount.setUserNo(userNo);
        userAccount.setUserName(userName);
        userAccount.setStaffStatus(staffStatus);
        userAccount.setLastLoginTime(new Date());
        userAccount.setAvatarUrl(avatar);
        userAccount.setSeid(seid);
        UpdateWrapper<UserAccount> updateWrapper = new UpdateWrapper<UserAccount>()
                .eq("user_id", userId);
        saveOrUpdate(userAccount, updateWrapper);
    }*/

    @Override
    public UserCacheDto getApassOnlineUser(String userId) {
        String token = generateToken(userId);
        UserCacheDto userCacheDto = redisService.getHashValue(getApassUserCacheKey(token), UserCacheDto.class);
        return userCacheDto;
    }


    public void logout(String currentUserId) {
        if (StringUtils.isEmpty(currentUserId)) {
            return;
        }

        UserAccount userAccount = new UserAccount();
//        userAccount.setUserId(currentUserId);
        userAccount.setLastLogoutTime(new Date());
        UpdateWrapper<UserAccount> updateWrapper = new UpdateWrapper<UserAccount>()
                .eq("user_id", currentUserId);
        saveOrUpdate(userAccount, updateWrapper);

        String cacheKey = RedisCacheKey.ONLINE_USER_CACHE + currentUserId;
        redisService.delKey(cacheKey);

        List<String> userIds = new ArrayList<>();
        userIds.add(currentUserId);
        cleanUserGroupCache(userIds);
    }


    @Override
    public Boolean isOnLine(String userId) {
        String cacheKey = RedisCacheKey.ONLINE_USER_CACHE + userId;
        Boolean hasKye = redisService.hasKey(cacheKey);
        return hasKye;
    }

    /**
     * 只有登录时才会调用，初始化用户的缓存数据
     *
     * @param userAccount
     * @return
     */
    @Override
    public UserCacheDto cacheUserInLogin(UserAccount userAccount) {
        String userId = userAccount.getUserId();
        String key = RedisCacheKey.ONLINE_USER_CACHE + userId;
        UserCacheDto userCacheDto = new UserCacheDto();
        userCacheDto = BeanUtil.copyProperties(userAccount, UserCacheDto.class);
        redisService.putHashAll(key, BeanUtil.beanToMap(userCacheDto));
        return userCacheDto;
    }

    @Override
    public UserCacheDto cacheApassUserInLogin(UserAccount userAccount) {
        UserCacheDto userCacheDto = new UserCacheDto();
        userCacheDto = BeanUtil.copyProperties(userAccount, UserCacheDto.class);
        String token = generateToken(userAccount.getUserId());
        userCacheDto.setToken(token);
        String key = getApassUserCacheKey(token);
        redisService.putHashAll(key, BeanUtil.beanToMap(userCacheDto));
        return userCacheDto;
    }


    @Override
    public UserCacheDto getByToken(String token) {
        UserCacheDto userCacheDto = redisService.getHashValue(getApassUserCacheKey(token), UserCacheDto.class);
        return userCacheDto;
    }

    private String generateToken(String userId) {
        String token = MD5.create().digestHex(userId);
        return token;
    }

    private String getApassUserCacheKey(String token) {
        return APASS_USER_CACHE + RedisCacheKey.SUFFIX + token;
    }


    @Override
    public void cleanUserGroupCache(List<String> userIds) {
        try {
            List<String> keys = new ArrayList<>();
            for (String userId : userIds) {
                keys.add(RedisCacheKey.ONLINE_USER_GROUP_CACHE + userId);
            }
            redisService.delKeys(keys);
        } catch (Exception e) {
            log.error("更新缓存中群成员信息异常,userId{} ", JSONUtil.toJsonStr(userIds), e);
        }
    }

    /**
     * @param userId
     * @return
     */
    @Override
    public String getAvatarUrl(String userId) {
        String url;
        if (StringUtils.isEmpty(userId)) {
            return null;
        }
        UserAccount userAccount = getOne(new QueryWrapper<UserAccount>().lambda().eq(UserAccount::getUserId, userId));
        if (null == userAccount) {
            return null;
        }

        url = kuailuApiUrl + "dlfile?seid=" + userAccount.getSeid() + "&appid=" + userPortalAppId + "&store=_default&groupValue=" + "&subCatalog=" + userId;
        return url;
    }


    @Override
    public String getAvatarUrl(String userId, Integer chatType) {
        String url = "";
        if (ChatType.CHAT_TYPE_PRIVATE.getNumber() == chatType) {
            UserAccount userAccount = getOne(new QueryWrapper<UserAccount>().lambda().eq(UserAccount::getUserId, userId));
            url = kuailuApiUrl + "dlfile?seid=" + userAccount.getSeid() + "&appid=" + userPortalAppId + "&store=_default&groupValue=" + "&subCatalog=" + userId;
        }
        return url;
    }


    /**
     * @param userId
     * @return
     */
    @Override
    public String getAvatarUrl(String userId, String seid) {
        String url = kuailuApiUrl + "dlfile?seid=" + seid + "&appid=" + userPortalAppId + "&store=_default&groupValue=" + "&subCatalog=" + userId;
        return url;
    }


    @Override
    public String getAvatarUrl(String userId, Integer chatType, String seid) {
        String url = null;
        if (ChatType.CHAT_TYPE_PRIVATE.getNumber() == chatType) {
            url = kuailuApiUrl + "dlfile?seid=" + seid + "&appid=" + userPortalAppId + "&store=_default&groupValue=" + "&subCatalog=" + userId;
        } else {

        }
        return url;
    }

    @Override
    public UserAccount getByUserId(String userId) {
        LambdaQueryWrapper queryWrapper = new QueryWrapper<UserAccount>()
                .lambda()
                .eq(UserAccount::getUserId, userId);
        return getOne(queryWrapper);
    }

    private void updateAvatarByUserId(String userId, String url) {
        LambdaUpdateWrapper updateWrapper = new UpdateWrapper<UserAccount>().set("avatar_url", url).lambda().eq(UserAccount::getUserId, userId);
        update(updateWrapper);
    }

}
