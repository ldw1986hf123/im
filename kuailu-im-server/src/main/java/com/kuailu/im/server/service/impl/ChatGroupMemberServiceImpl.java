package com.kuailu.im.server.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kuailu.im.core.packets.ChatType;
import com.kuailu.im.core.packets.PrivateGroupDto;
import com.kuailu.im.server.constant.RedisCacheKey;
import com.kuailu.im.server.dto.GroupCacheDto;
import com.kuailu.im.server.mapper.ChatGroupMemberMapper;
import com.kuailu.im.server.model.entity.ChatGroup;
import com.kuailu.im.server.model.entity.ChatGroupMember;
import com.kuailu.im.server.service.IChatGroupMemberService;
import com.kuailu.im.server.service.IChatGroupService;
import com.kuailu.im.server.util.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
@Service
@Slf4j
public class ChatGroupMemberServiceImpl extends ServiceImpl<ChatGroupMemberMapper, ChatGroupMember> implements IChatGroupMemberService {
    @Autowired(required = false)
    @Lazy
    IChatGroupService groupService;

    @Autowired
    RedisService redisService;


    @Override
    public PrivateGroupDto getPrivateGroup(String userId, String memberId) {
        return baseMapper.getPrivateGroup(userId, memberId);

    }

    @Override
    public ChatGroupMember getNewGroupName(String groupId) {
        QueryWrapper<ChatGroupMember> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ChatGroupMember::getGroupId, groupId);
        queryWrapper.orderByAsc("id").last("limit 1");
        List<ChatGroupMember> chatGroupMembers = this.baseMapper.selectList(queryWrapper);
        if (chatGroupMembers != null && chatGroupMembers.size() > 0) {
            return chatGroupMembers.get(0);
        }
        return null;
    }

    @Override
    public Map<String, ChatGroupMember> getPrivateChatterMember(List<String> groupIdList, String currentUserId) {
        Map<String, ChatGroupMember> groupIdToGroupMember = new HashMap<>();
        QueryWrapper queryWrapper = new QueryWrapper<ChatGroupMember>()
                .select("user_id,group_id,user_name,role_type")
                .groupBy("user_id,group_id,user_name,role_type")
                .in("group_id", groupIdList)
                .ne("user_id", currentUserId);

        List<ChatGroupMember> resultList = list(queryWrapper);
        getSelfChat(groupIdList, resultList);

        groupIdToGroupMember = resultList.stream().collect(Collectors.toMap(ChatGroupMember::getGroupId, groupMember -> groupMember));
        return groupIdToGroupMember;
    }


    @Override
    public List<ChatGroupMember> getAllMembers(String groupId) {
        List<ChatGroupMember> memberList = getGroupMemberCache(groupId);
        return memberList;
    }


    @Override
    public String getRoleTypeInPrivateChat(String userId, String groupId) {
        String roleType = "";
        try {
            List<ChatGroupMember> memberList = getGroupMemberCache(groupId);
            roleType = memberList.stream().filter(item -> item.getUserId().equals(userId)).findAny().get().getRoleType();
        } catch (Exception e) {
            log.error("获取在会话中角色异常,groupId {} userId:{}", groupId, userId, e);
        }

        return roleType;
    }


    @Override
    public List<String> getAllOnlineMembers(String groupId) {
        List<String> userIdList = new ArrayList<>();
        List<ChatGroupMember> memberList = getGroupMemberCache(groupId);
        for (ChatGroupMember chatGroupMember : memberList) {
            userIdList.add(chatGroupMember.getUserId());
        }
        return userIdList;
    }


    private List<ChatGroupMember> getGroupMemberCache(String groupId) {
        String key = RedisCacheKey.GROUP_MEMBER_CACHE + groupId;
        List<ChatGroupMember> groupMemberList = redisService.range(key, ChatGroupMember.class);
        if (CollectionUtils.isEmpty(groupMemberList)) {
            groupMemberList = list(new QueryWrapper<ChatGroupMember>()
                    .select("group_id", "user_id", "user_name", "user_no", "role_type", "created_time")
                    .lambda()
                    .eq(ChatGroupMember::getGroupId, groupId));
            redisService.addZSet(key, groupMemberList);
        }
        return groupMemberList;
    }

    @Override
    public void cleanGroupMemberCache(String groupId) {
        try {
            String key = RedisCacheKey.GROUP_MEMBER_CACHE + groupId;
            redisService.delKey(key);
        } catch (Exception e) {
            log.error("更新缓存中群成员信息异常,groupId {} ", groupId, e);
        }
    }

    @Override
    public List<String> getAllJoinGroupId(String userId) {
        List<String> groupIdList = new ArrayList<>();
        LambdaQueryWrapper queryWrapper = new QueryWrapper<ChatGroupMember>()
                .select("group_id").lambda()
                .eq(ChatGroupMember::getUserId, userId)
                .groupBy(ChatGroupMember::getGroupId);
        List<ChatGroupMember> groupMemberList = list(queryWrapper);
        if (CollectionUtils.isNotEmpty(groupMemberList)) {
            groupIdList = groupMemberList.stream().map(ChatGroupMember::getGroupId).collect(Collectors.toList());
        }
        return groupIdList;
    }

    public Boolean isInGroup(String groupId, String userId) {
        List<GroupCacheDto> groupCacheDtoLis = getUserGroups(userId);
        if (CollectionUtils.isNotEmpty(groupCacheDtoLis)) {
            return groupCacheDtoLis.stream().map(GroupCacheDto::getGroupId).collect(Collectors.toList()).contains(groupId);
        } else {
            LambdaQueryWrapper lambdaQueryWrapper = new QueryWrapper<ChatGroupMember>().lambda().eq(ChatGroupMember::getGroupId, groupId).eq(ChatGroupMember::getUserId, userId);
            int count = count(lambdaQueryWrapper);
            if (count == 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void getSelfChat(List<String> groupIdList, List<ChatGroupMember> memberList) {
        QueryWrapper queryWrapper = new QueryWrapper<ChatGroupMember>()
                .select("group_id,user_id,user_name")
                .groupBy("group_id,user_id,user_name")
                .in("group_id", groupIdList)
                .having("COUNT( user_id ) > 1");

        ChatGroupMember selfChatMember = getOne(queryWrapper);
        if (null != selfChatMember) {
            memberList.add(selfChatMember);
        }
    }

    public List<GroupCacheDto> getUserGroups(String userId) {
        String key = RedisCacheKey.ONLINE_USER_GROUP_CACHE + userId;
        List<GroupCacheDto> groupCacheDtoList = redisService.range(key, GroupCacheDto.class);
        if (CollectionUtils.isEmpty(groupCacheDtoList)) {
            List<String> allGroupIds = getAllJoinGroupId(userId);
            if (CollectionUtils.isNotEmpty(allGroupIds)) {
                LambdaQueryWrapper queryWrapper = new QueryWrapper<ChatGroup>()
                        .select("group_id", "group_name", "group_owner", "created_time", "avatar", "chat_type")
                        .lambda().in(ChatGroup::getGroupId, allGroupIds);
                List<ChatGroup> chatGroupList = groupService.list(queryWrapper);
                groupCacheDtoList = JSONUtil.toList(JSONUtil.toJsonStr(chatGroupList), GroupCacheDto.class);
                redisService.addZSet(key, groupCacheDtoList);
            }
        }

        for (GroupCacheDto groupCacheDto : groupCacheDtoList) {
            String groupId = groupCacheDto.getGroupId();
            if (ChatType.CHAT_TYPE_PRIVATE.getNumber() == groupCacheDto.getChatType() || ChatType.CHAT_TYPE_MSG_HELPER.getNumber() == groupCacheDto.getChatType()) {
                List<ChatGroupMember> chatGroupMemberList = getGroupMemberCache(groupId);
                ChatGroupMember groupMember = getGroupMemberCache(groupId).stream().filter(chatGroupMember -> !userId.equals(chatGroupMember.getUserId())).findAny()
                        .orElse(chatGroupMemberList.get(0));
                if (null == groupMember) {
                    log.error("数据有误，私聊会话，找不到对话人的数据。userId：{}   groupId:{}", userId, groupId);
                } else {
                    groupCacheDto.setPrivateChatMember(groupMember);
                }
            }
        }
        return groupCacheDtoList;
    }

    public List<GroupCacheDto> cacheLoginUserGroups(String userId) {
        List<GroupCacheDto> groupCacheDtoList = new ArrayList<>();
        try {
            List<String> allGroupIds = getAllJoinGroupId(userId);
            LambdaQueryWrapper queryWrapper = new QueryWrapper<ChatGroup>()
                    .select("group_id", "group_name", "group_owner", "created_time", "avatar", "chat_type")
                    .lambda()
                    .in(ChatGroup::getGroupId, allGroupIds);
            if (CollectionUtils.isEmpty(allGroupIds)) {
                log.info("用户没有任何会话，userId：{}", userId);
                return groupCacheDtoList;
            }
            List<ChatGroup> chatGroupList = groupService.list(queryWrapper);

            for (ChatGroup chatGroup : chatGroupList) {
                GroupCacheDto groupDto = new GroupCacheDto();
                String groupId = chatGroup.getGroupId();
                groupDto.setGroupId(groupId);
                groupDto.setGroupName(chatGroup.getGroupName());
                groupDto.setGroupOwner(chatGroup.getGroupOwner());
                groupDto.setCreatedTime(chatGroup.getCreatedTime());
                groupDto.setAvatar(chatGroup.getAvatar());
                groupDto.setChatType(chatGroup.getChatType());
                groupCacheDtoList.add(groupDto);
            }
            //初始化群成员缓存
            setGroupMemberCache(groupCacheDtoList, userId);

            redisService.addZSet(RedisCacheKey.ONLINE_USER_GROUP_CACHE + userId, groupCacheDtoList);
        } catch (Exception e) {
            log.error("登录初始化群异常 userId:{}", userId, e);
        }
        return groupCacheDtoList;
    }


    private void setGroupMemberCache(List<GroupCacheDto> groupCacheDtoList, String userId) {
        List<String> groupIdListPrivateGroup = groupCacheDtoList.stream()
                .filter(chatGroup -> chatGroup.getChatType() == ChatType.CHAT_TYPE_PRIVATE.getNumber())
                .map(GroupCacheDto::getGroupId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(groupIdListPrivateGroup)) {
            log.info("userID:{} 没有私聊群", userId);
            return;
        }
        Map<String, ChatGroupMember> groupIdToMember = getPrivateChatterMember(groupIdListPrivateGroup, userId);

        for (GroupCacheDto groupCacheDto : groupCacheDtoList) {
            String groupId = groupCacheDto.getGroupId();
            if (ChatType.CHAT_TYPE_PRIVATE.getNumber() == groupCacheDto.getChatType()) {
                ChatGroupMember groupMember = groupIdToMember.get(groupCacheDto.getGroupId());
                if (null == groupMember) {
                    log.error("数据有误，私聊会话，找不到对话人的数据。singleGroupId：{}", groupId);
                } else {
                    groupCacheDto.setPrivateChatMember(groupIdToMember.get(groupId));
                }
            }
        }
    }

    public void cleanUserUserGroupCache(String userId) {
        String key = RedisCacheKey.ONLINE_USER_GROUP_CACHE + userId;
        redisService.delKey(key);
    }

    @Override
    public Boolean isSelfChat(String groupId) {
        try {
            List<ChatGroupMember> groupMemberList = list(new QueryWrapper<ChatGroupMember>().lambda().eq(ChatGroupMember::getGroupId, groupId));
            if (CollectionUtils.isNotEmpty(groupMemberList) && groupMemberList.size() == 2) {
                if (groupMemberList.get(0).getUserId().equals(groupMemberList.get(1).getUserId())) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("判断是否自己与自己会话异常 groupId：{}", groupId, e);
        }
        return false;
    }

    @Override
    public void saveGroupMemberBatch(List<ChatGroupMember> inviteMembers) {
/*        inviteMembers.stream()
                .map(member -> {
                    member.setRoleType(MemberRoleType.MEMBER.getCode()); // 设置对象的属性
                    member.setCreatedTime(now);
                    member.setUpdatedTime(now);
                    return member;
                })
                .collect(Collectors.toList());*/
        baseMapper.saveGroupMemberBatch(inviteMembers);
    }

    @Override
    public ChatGroupMember getPrivateChatter(String userId, String groupId) {
        ChatGroupMember chatter = null;
        try {
            QueryWrapper<ChatGroupMember> queryWrapper = new QueryWrapper<ChatGroupMember>().eq("group_id", groupId);
            if (isSelfChat(groupId)) {
                String roleType = getRoleTypeInPrivateChat(userId, groupId);
                //是自己跟自己的会话
                queryWrapper.ne("role_type", roleType);
            } else {
                queryWrapper.ne("user_id", userId);
            }
            chatter = getOne(queryWrapper);
        } catch (Exception e) {
            log.error("获取私聊中对应用户信息异常，数据有误userId：{},groupId:{}", userId, groupId);
        }
        if (null == chatter) {
            log.error("获取私聊中对应用户信息异常，数据有误userId：{},groupId:{}", userId, groupId);
        }
        return chatter;
    }


}
