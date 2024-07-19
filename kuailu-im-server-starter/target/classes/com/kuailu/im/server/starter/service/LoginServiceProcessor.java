package com.kuailu.im.server.starter.service;

import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.packets.UserDto;
import com.kuailu.im.server.processor.login.LoginCmdProcessor;
import com.kuailu.im.server.protocol.AbstractProtocolCmdProcessor;
import com.kuailu.im.server.util.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 */
@Deprecated
@Slf4j
public class LoginServiceProcessor extends AbstractProtocolCmdProcessor implements LoginCmdProcessor {

    @Autowired
    RedisService redisService;

    @Override
    @Transactional(rollbackFor = {RuntimeException.class, Exception.class})
    public void onSuccess(UserDto currentUser, String seid, ImChannelContext channelContext) {
        log.info("{} 登录成功回调方法", currentUser.getUserName());
//        getUserGroup(currentUser, seid);
//        initGroup(currentUser);
    }

    @Override
    public void onFailed(ImChannelContext imChannelContext, String userId, String seid) {
        log.info("userId:{}   seid:{}  登录失败回调方法", userId, seid);
    }

    /**
     * 初始化绑定或者解绑群组;
     */
  /*  private void initGroup(UserDto currentUser) {
        try {
            List<GroupDto> groupsInDatabase = currentUser.getGroups();
            if (CollectionUtils.isEmpty(groupsInDatabase)) {
                return;
            }

            //绑定群组
            for (GroupDto groupDto : groupsInDatabase) {
                Set<String> userIdSet = new HashSet<>(groupDto.getUsers().stream().map(UserDto::getUserId).collect(Collectors.toList()));
                userIdSet.add(currentUser.getUserId());
                JimServerAPI.bindUserListToGroup(userIdSet, groupDto.getGroupId());
            }
        } catch (Exception e) {
            log.error("登录完成后，绑定群组异常.userDto:{}", JSONUtil.toJsonStr(currentUser), e);
        }
    }*/


   /* private void getUserGroup(UserDto userDto, String seid) {
        try {
            String userId = userDto.getUserId();
            RedisService redisService = ApplicationContextHelper.get().getBean(RedisService.class);

            //先从缓存，获取用户所有的groupId
            List<String> groupIdList = new ArrayList<>();
            UserCacheDto userCacheDto = redisService.getValue(RedisCacheKey.ONLINE_USER_CACHE + userId, UserCacheDto.class);
            IChatGroupMemberService groupMemberService = ApplicationContextHelper.get().getBean(IChatGroupMemberService.class);
            IChatGroupService groupService = ApplicationContextHelper.get().getBean(IChatGroupService.class);

            if (null != userCacheDto) {
                groupIdList = userCacheDto.getGroups().stream().map(GroupCacheDto::getGroupId).collect(Collectors.toList());
            } else {
                List<ChatGroupMember> chatGroupList = groupMemberService.getAllJoinGroupId(userDto.getUserId());
                groupIdList = chatGroupList.stream().map(ChatGroupMember::getGroupId).collect(Collectors.toList());
                List<ChatGroup> chatGroups = groupService.list(new QueryWrapper<ChatGroup>().lambda().in(ChatGroup::getGroupId, groupIdList));
                List<GroupDto> groups = new ArrayList<>();
                for (ChatGroup chatGroup : chatGroups) {
                    GroupDto groupDto = new GroupDto();
                    groupDto.setGroupId(chatGroup.getGroupId());
                    groupDto.setGroupName(chatGroup.getGroupName());
                    groupDto.setGroupOwner(chatGroup.getGroupOwner());
                    groupDto.setAvatar(chatGroup.getAvatar());
                    groups.add(groupDto);
                }
                userDto.setGroups(groups);
            }

         *//*   if (CollectionUtils.isEmpty(groupIdList)) {
                return;
            }*//*

            Map<String, List<ChatGroupMember>> groupIdToMemberList = groupMemberService.getMemberByGroupId(groupIdList);
            Map<String, List<UserDto>> groupIdToUserDtoList = new HashMap<>();
            for (String groupId : groupIdToMemberList.keySet()) {
                List<ChatGroupMember> memberList = groupIdToMemberList.get(groupId);
                List<UserDto> userDtoList = JSONUtil.toList(JSONUtil.toJsonStr(memberList), UserDto.class);
                groupIdToUserDtoList.put(groupId, userDtoList);
            }

//            List<GroupDto> groupDtos = new ArrayList<>();
            List<ImChannelContext> userChannelContext = JimServerAPI.getByUserId(userId);
            for (String groupId : groupIdList) {
//                String groupId = row.getGroupId();
             *//*   GroupDto groupDto = new GroupDto();
                groupDto.setUsers(groupIdToUserDtoList.get(groupId));
                groupDto.setGroupId(groupId);
                groupDtos.add(groupDto);*//*
                List<ImChannelContext> groupChannelContext = JimServerAPI.getByGroup(groupId);
                for (ImChannelContext singleUserChannelContext : userChannelContext) {
                    if (groupChannelContext.contains(singleUserChannelContext)) {
                        log.info("用户已经的终端已经在群聊中，不再进行绑定:groupId:{} userId:{},terminal node:{}", groupId, singleUserChannelContext.getUserId(), singleUserChannelContext.getClientNode().toString());
                    } else {
                        JimServerAPI.bindGroup(singleUserChannelContext, groupId);
                    }
                }
            }
        } catch (Exception e) {
            log.error("登录完成后，设置用户所属group发生异常.userDto:{}", JSONUtil.toJsonStr(userDto), e);
        }
    }*/
}
