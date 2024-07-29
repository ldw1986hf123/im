package com.kuailu.im.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.ImStatus;
import com.kuailu.im.core.exception.AppException;
import com.kuailu.im.core.packets.*;
import com.kuailu.im.server.JimServerAPI;
import com.kuailu.im.server.enums.ChatGroupStatus;
import com.kuailu.im.server.enums.MemberRoleType;
import com.kuailu.im.server.mapper.ChatGroupMapper;
import com.kuailu.im.server.model.entity.*;
import com.kuailu.im.server.mq.PushMessage;
import com.kuailu.im.server.service.*;
import com.kuailu.im.server.util.RedisService;
import com.kuailu.im.server.util.UUIDUtil;
import com.kuailu.im.server.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.kuailu.im.server.constant.RedisCacheKey.CONVERSATION_ID_LIST_CACHE;

/**
 *
 */
@Service
@Slf4j
public class ChatGroupServiceImpl extends ServiceImpl<ChatGroupMapper, ChatGroup> implements IChatGroupService {

    @Autowired(required = false)
    @Lazy
    private IChatGroupMemberService chatGroupMemberService;

    @Autowired(required = false)
    @Lazy
    private IConversationService conversationService;

    @Autowired(required = false)
    @Lazy
    private IUserAccountService userAccountService;


    @Autowired
    RedisService redisService;

    @Autowired
    IWhiteListMemberService whiteListMemberService;

    @Autowired
    IWhiteListService whiteListService;

    @Autowired
    RedisTemplate redisTemplate;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ChatGroupDetailVo saveOrUpdateChatGroup(ChatGroupVo chatGroupVo) {
        //更新群组名称
        String seid = chatGroupVo.getSeid();
        Date currentDateTime = new Date();
        String owner = chatGroupVo.getOwner();
        String groupId = chatGroupVo.getGroupId();
        /*********************************************************************修改群组  改群名和群头像的时候，才会进入这里，其他都不会********************************************************************/
        if (StringUtils.isNotEmpty(chatGroupVo.getGroupId())) {

            QueryWrapper<ChatGroup> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(ChatGroup::getGroupId, groupId);
            ChatGroup chatGroup = getOne(queryWrapper);
            //
            UpdateWrapper updateWrapper = new UpdateWrapper<Conversation>().eq("chatgroup_id", groupId);
            if (StringUtils.isNotEmpty(chatGroup.getGroupName())) {
                chatGroup.setGroupName(chatGroupVo.getGroupName());
                updateWrapper.set("conversation_name", chatGroupVo.getGroupName());
            }
            chatGroup.setUpdatedTime(currentDateTime);

            if (StringUtils.isNotEmpty(chatGroupVo.getAvatar())) {
                chatGroup.setAvatar(chatGroupVo.getAvatar());
                updateWrapper.set("avatar", chatGroupVo.getAvatar());
            }
            if (StringUtils.isNotEmpty(chatGroupVo.getUpdateUser())) {
                chatGroup.setUpdatedBy(chatGroupVo.getUpdateUser());
            }
            updateById(chatGroup);
            String conversationId = conversationService.getConversationIdByGroupId(groupId, owner);
            conversationService.update(updateWrapper);
            conversationService.cleanContentCache(owner, conversationId);

            ChatGroupDetailVo chatGroupDetailVo = ChatGroupDetailVo.builder().build();
            BeanUtil.copyProperties(chatGroup, chatGroupDetailVo);
            chatGroupDetailVo.setId(String.valueOf(chatGroup.getId()));
            chatGroupDetailVo.setUpdatedTime(currentDateTime.getTime());

            chatGroupDetailVo.setConversationId(conversationId);
            return chatGroupDetailVo;
        }

        /*********************************************************************新增群组********************************************************************/
        ChatGroupDetailVo chatGroupDetailVo = ChatGroupDetailVo.builder().build();
        Integer chatType = chatGroupVo.getChatType();
        String newGroupId = UUIDUtil.getUUID();
        List<String> deptIds = chatGroupVo.getDeptIds();
        List<ChatGroupMember> sourceMemberList = JSONUtil.toList(JSONUtil.toJsonStr(chatGroupVo.getMembers()), ChatGroupMember.class);
//        List<ChatGroupMember> chatGroupMembers = new ArrayList<>();
        ChatGroup chatGroup = new ChatGroup();
        List<ChatGroupMember> totalMemberList = mergeDeptUsers(sourceMemberList, seid, deptIds, newGroupId);

        /******************************校验建群条件*****************************/
        if (!chekCreateChatGroup(totalMemberList, chatType)) {
            log.error("校验建群条件 不通过");
            return chatGroupDetailVo;
        }
        /******************************校验建群条件*****************************/

        if (chatType == ChatType.CHAT_TYPE_PRIVATE.getNumber()) {
            PrivateGroupDto privateGroupDto = existPrivateGroup(sourceMemberList.get(0).getUserId(), sourceMemberList.get(1).getUserId());
            if (null != privateGroupDto) {
                QueryWrapper<ChatGroup> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().eq(ChatGroup::getGroupId, privateGroupDto.getGroupId());
                chatGroup = getOne(queryWrapper);
                chatGroupDetailVo = ChatGroupDetailVo.builder().build();
                BeanUtil.copyProperties(chatGroup, chatGroupDetailVo);
                //查询conversationId
                chatGroupDetailVo.setConversationId(conversationService.getConversationIdByGroupId(privateGroupDto.getGroupId(), owner));
                return chatGroupDetailVo;
            }
            chatGroup.setGroupName("");
            totalMemberList = constructPrivateMember(newGroupId, owner, totalMemberList);
        } else if (chatType == ChatType.CHAT_TYPE_PUBLIC.getNumber()) {
            totalMemberList = totalMemberList.stream()
                    .peek(chatGroupMemberVo -> chatGroupMemberVo.setRoleType(chatGroupMemberVo.getUserId().equals(owner) ? MemberRoleType.OWNER.getCode() : MemberRoleType.MEMBER.getCode()))
                    .collect(Collectors.toList());
            chatGroup.setGroupName(generateChatGroupName(totalMemberList));
        }

        chatGroup.setGroupId(newGroupId)
                .setGroupOwner(owner)
                .setChatType(chatType)
                .setCreatedBy(owner)
                .setStatus(ChatGroupStatus.NORMAL.getCode())
                .setUpdatedBy(owner)
                .setCreatedTime(currentDateTime)
                .setUpdatedTime(currentDateTime);

        /************************************保存群组成员***********************************/

        save(chatGroup);
        chatGroupMemberService.saveBatch(totalMemberList);
        //生成会话数据
        String conversationId = conversationService.generateConversationList(totalMemberList, chatGroup);

        BeanUtil.copyProperties(chatGroup, chatGroupDetailVo);
        chatGroupDetailVo.setId(String.valueOf(chatGroup.getId()));
        chatGroupDetailVo.setCreatedTime(currentDateTime.getTime());
        chatGroupDetailVo.setUpdatedTime(currentDateTime.getTime());
        chatGroupDetailVo.setConversationId(conversationId);
        return chatGroupDetailVo;
    }

    /**
     * 根据用户，判断私聊群是否已经存在。存在则返回true,否则返回false
     *
     * @param userId1
     * @param userId2
     * @return
     */
    private PrivateGroupDto existPrivateGroup(String userId1, String userId2) {
        PrivateGroupDto privateGroupDto = chatGroupMemberService.getPrivateGroup(userId1, userId2);
              /*  QueryWrapper<ChatGroup> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().eq(ChatGroup::getGroupId, privateGroupDto.getGroupId());
                ChatGroup chatGroup = getOne(queryWrapper);
                ChatGroupDetailVo chatGroupDetailVo = ChatGroupDetailVo.builder().build();
                BeanUtil.copyProperties(chatGroup, chatGroupDetailVo);
                //查询conversationId
                chatGroupDetailVo.setConversationId(conversationService.getConversationIdByGroupId(chatGroup.getGroupId(), owner));*/
        return privateGroupDto;
    }

    private String generateChatGroupName(List<ChatGroupMember> totalMemberList) {
        List<String> nameList = totalMemberList.stream().map(ChatGroupMember::getUserName).collect(Collectors.toList());
        String groupName = String.join(",", nameList);
        if (groupName.length() > 30) {
            groupName = groupName.substring(0, 30);
        }
        return groupName;
    }


    @Override
    public ChatGroupDetailVo getDetailsByGroupId(String groupId, String userId) {
        QueryWrapper<ChatGroup> queryWrapper = new QueryWrapper<>();
//        queryWrapper.select(ChatGroup.class, info -> !info.getColumn().equals("start_mute_time"));
        queryWrapper.lambda().eq(ChatGroup::getGroupId, groupId);
        ChatGroup chatGroup = getOne(queryWrapper);

        ChatGroupDetailVo chatGroupDetailVo = ChatGroupDetailVo.builder().build();
        BeanUtil.copyProperties(chatGroup, chatGroupDetailVo, false);
        chatGroupDetailVo.setId(groupId);
//        chatGroupDetailVo.setCreatedTime(chatGroup.getCreatedTime().getTime());
        chatGroupDetailVo.setGroupOwner(chatGroup.getGroupOwner());

/*        QueryWrapper<ChatGroupMember> chatGroupMemberQueryWrapper = new QueryWrapper<>();
        chatGroupMemberQueryWrapper.lambda().eq(ChatGroupMember::getGroupId, groupId);*/
        List<ChatGroupMember> chatGroupMemberList = chatGroupMemberService.getAllMembers(groupId);
        List<GroupDetailMemberVo> groupDetailMemberVoList = JSONUtil.toList(JSONUtil.toJsonStr(chatGroupMemberList), GroupDetailMemberVo.class);
        chatGroupDetailVo.setMembers(groupDetailMemberVoList);

        QueryWrapper<Conversation> conversationQueryWrapper = new QueryWrapper<>();
        conversationQueryWrapper.select("conversation_id").lambda()
                .eq(Conversation::getUserId, userId)
                .eq(Conversation::getChatgroupId, groupId);
        Conversation conversation = conversationService.getOne(conversationQueryWrapper);
        chatGroupDetailVo.setConversationId(conversation.getConversationId());
        return chatGroupDetailVo;
    }

    @Override
    public ChatGroupDetailVo getDetailsByGroupId(String groupId) {
        QueryWrapper<ChatGroup> queryWrapper = new QueryWrapper<>();
        queryWrapper.select(ChatGroup.class, info -> !info.getColumn().equals("start_mute_time"));
        queryWrapper.lambda().eq(ChatGroup::getGroupId, groupId);
        ChatGroup chatGroup = getOne(queryWrapper);

        ChatGroupDetailVo chatGroupDetailVo = ChatGroupDetailVo.builder().build();
        BeanUtil.copyProperties(chatGroup, chatGroupDetailVo, false);
        chatGroupDetailVo.setId(groupId);
        chatGroupDetailVo.setCreatedTime(chatGroup.getCreatedTime().getTime());
        chatGroupDetailVo.setGroupOwner(chatGroup.getGroupOwner());

/*        QueryWrapper<ChatGroupMember> chatGroupMemberQueryWrapper = new QueryWrapper<>();
        chatGroupMemberQueryWrapper.lambda().eq(ChatGroupMember::getGroupId, groupId);*/
        List<ChatGroupMember> chatGroupMemberList = chatGroupMemberService.getAllMembers(groupId);
        List<GroupDetailMemberVo> groupDetailMemberVoList = JSONUtil.toList(JSONUtil.toJsonStr(chatGroupMemberList), GroupDetailMemberVo.class);
        chatGroupDetailVo.setMembers(groupDetailMemberVoList);

        QueryWrapper<Conversation> conversationQueryWrapper = new QueryWrapper<>();
        conversationQueryWrapper.select("conversation_id").lambda().eq(Conversation::getChatgroupId, groupId);
        Conversation conversation = conversationService.getOne(conversationQueryWrapper);
        chatGroupDetailVo.setConversationId(conversation.getConversationId());
        return chatGroupDetailVo;
    }


  /*  @Override
    public ChatGroupDetailVo getDetailsByGroupId(String seid, String groupId) {
        QueryWrapper<ChatGroup> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ChatGroup::getGroupId, groupId);
        ChatGroup chatGroup = getOne(queryWrapper);
        if (chatGroup == null) {
            throw new AppException(ImStatus.C10023.getCode(), ImStatus.C10023.getDescription());
        }
        ChatGroupDetailVo chatGroupDetailVo = ChatGroupDetailVo.builder().build();
        BeanUtil.copyProperties(chatGroup, chatGroupDetailVo, false);
        chatGroupDetailVo.setId(String.valueOf(chatGroup.getId()));
        chatGroupDetailVo.setCreatedTime(chatGroup.getCreatedTime().getTime());
        chatGroupDetailVo.setUpdatedTime(chatGroup.getUpdatedTime().getTime());

        QueryWrapper<ChatGroupMember> chatGroupMemberQueryWrapper = new QueryWrapper<>();
        chatGroupMemberQueryWrapper.lambda().eq(ChatGroupMember::getGroupId, groupId);
        List<ChatGroupMember> chatGroupMemberList = chatGroupMemberService.list(chatGroupMemberQueryWrapper);

        List<GroupDetailMemberVo> groupDetailMemberVoList = new ArrayList();
        //todo 获取群成员在线状态,应该优化成批量获取的
        for (ChatGroupMember groupMember : chatGroupMemberList) {
            GroupDetailMemberVo detailMemberVo = BeanUtil.copyProperties(groupMember, GroupDetailMemberVo.class);
            UserInfoDetail userInfoDetail = passService.getUserDetailsByUserId(seid, groupMember.getUserId());
            detailMemberVo.setStaffStatus(userInfoDetail.getStaffStatus());
            detailMemberVo.setStaffStatusName(userInfoDetail.getStaffStatusName());
            detailMemberVo.setStatusDesc(userInfoDetail.getStatusDesc());
            groupDetailMemberVoList.add(detailMemberVo);
        }
        chatGroupDetailVo.setMembers(groupDetailMemberVoList);

        QueryWrapper<Conversation> conversationQueryWrapper = new QueryWrapper<>();
        conversationQueryWrapper.lambda().eq(Conversation::getChatgroupId, groupId);
        Conversation conversation = conversationService.getOne(conversationQueryWrapper);
        chatGroupDetailVo.setConversationId(conversation.getConversationId());
        return chatGroupDetailVo;
    }*/


    /**
     * 邀请信成员进群
     *
     * @param operationVO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatGroupDetailVo memberInviteChatGroup(ChatGroupMemberOperationVO operationVO) {
        String groupId = operationVO.getGroupId();
        String updateUser = operationVO.getUpdateUser();
        List<ChatGroupMember> sourceMemberList = JSONUtil.toList(JSONUtil.toJsonStr(operationVO.getMembers()), ChatGroupMember.class);

        QueryWrapper<ChatGroup> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ChatGroup::getGroupId, groupId);
        ChatGroup chatGroup = getOne(queryWrapper);
        if (chatGroup == null) {
            throw new AppException(ImStatus.CANNOT_FIND_DATA.getCode(), ImStatus.CANNOT_FIND_DATA.getDescription());
        }

        //查询原有的群成员
        List<ChatGroupMember> inviteMembers = mergeDeptUsers(sourceMemberList, operationVO.getSeid(), operationVO.getDeptIds(), groupId);
        List<String> invitedUserIds = inviteMembers.stream().map(ChatGroupMember::getUserId).collect(Collectors.toList());
        int existedMember = chatGroupMemberService.count(new QueryWrapper<ChatGroupMember>().lambda()
                .eq(ChatGroupMember::getGroupId, groupId)
                .notIn(ChatGroupMember::getUserId, invitedUserIds));

        if ((existedMember + inviteMembers.size()) > 500) {
            throw new AppException(ImStatus.INVALID_VERIFICATION.getCode(), "群成员最多支持500人");
        }

        List<ChatGroupMember> memberExisted = new ArrayList<>();
        for (ChatGroupMember chatGroupMember : inviteMembers) {
            QueryWrapper<ChatGroupMember> memberQueryWrapper = new QueryWrapper<>();
            memberQueryWrapper.lambda().eq(ChatGroupMember::getGroupId, chatGroup.getGroupId());
            memberQueryWrapper.lambda().eq(ChatGroupMember::getUserId, chatGroupMember.getUserId());
            if (chatGroupMemberService.count(memberQueryWrapper) > 0) {
                log.info("groupId:{}  userId:{} 已在群里，不进行添加", groupId, chatGroupMember.getUserId());
                memberExisted.add(chatGroupMember);
            }
        }
        inviteMembers.removeAll(memberExisted);
//        chatGroupMemberService.saveBatch(chatGroupMembers);
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(inviteMembers)) {
            //保存成员信息
            Date now = new Date();
            inviteMembers.stream()
                    .map(member -> {
                        member.setRoleType(MemberRoleType.MEMBER.getCode()); // 设置对象的属性
                        member.setCreatedTime(now);
                        member.setUpdatedTime(now);
                        return member;
                    })
                    .collect(Collectors.toList());
            chatGroupMemberService.saveGroupMemberBatch(inviteMembers);
            //生成对应的会话
            conversationService.addConversationList(inviteMembers, chatGroup, updateUser);

            //情况缓存数据
            userAccountService.cleanUserGroupCache(invitedUserIds);
            conversationService.cleanUserConversationIdCache(invitedUserIds);
            chatGroupMemberService.cleanGroupMemberCache(groupId);
        }
        ChatGroupDetailVo chatGroupDetailVo = ChatGroupDetailVo.builder().build();

        chatGroupDetailVo.setConversationName(chatGroup.getGroupName());
        String conversationId = conversationService.getConversationIdByGroupId(groupId, updateUser);
        chatGroupDetailVo.setConversationId(conversationId);
        chatGroupDetailVo.setReceiver(groupId);
        BeanUtil.copyProperties(chatGroup, chatGroupDetailVo);
        return chatGroupDetailVo;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void memberRemoveChatGroup(ChatGroupMemberOperationVO operationVO) {
       /* if (StringUtils.isEmpty(operationVO.getGroupId()) || ArrayUtil.isEmpty(operationVO.getMembers())) {
            throw new AppException(ImStatus.INVALID_VERIFICATION.getCode(), ImStatus.INVALID_VERIFICATION.getDescription());
        }*/
        QueryWrapper<ChatGroup> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ChatGroup::getGroupId, operationVO.getGroupId());
        ChatGroup chatGroup = getOne(queryWrapper);
       /* if (chatGroup == null) {
            throw new AppException(ImStatus.CANNOT_FIND_DATA.getCode(), ImStatus.CANNOT_FIND_DATA.getDescription());
        }*/
        List<String> userList = operationVO.getMembers().stream().map(ChatGroupMemberVo::getUserId).collect(Collectors.toList());
        QueryWrapper<ChatGroupMember> chatGroupMemberQueryWrapper = new QueryWrapper<>();
        chatGroupMemberQueryWrapper.lambda().eq(ChatGroupMember::getGroupId, operationVO.getGroupId());
        chatGroupMemberQueryWrapper.lambda().in(ChatGroupMember::getUserId, userList);
        chatGroupMemberService.remove(chatGroupMemberQueryWrapper);

//        ImServerConfig imServerConfig = ImConfig.Global.get();
//        MessageHelper messageHelper = imServerConfig.getMessageHelper();
//        GroupDto groupDto = new GroupDto();
//        groupDto.setGroupId(operationVO.getGroupId());
//        GroupDto groupDto = GroupDto.builder().groupId(operationVO.getGroupId()).build();
        userList.forEach(t -> {
            //如果是群组的话，则获取
            if (t.equals(chatGroup.getGroupOwner())) {
                ChatGroupMember chatGroupMember = chatGroupMemberService.getNewGroupName(operationVO.getGroupId());
                if (chatGroupMember != null) {
                    chatGroupMember.setRoleType("owner");
                    chatGroupMemberService.updateById(chatGroupMember);
                    chatGroup.setGroupOwner(chatGroupMember.getUserId());
                    this.updateById(chatGroup);
//                    GroupDto cacheGroup = messageHelper.getGroupUsers(chatGroup.getGroupId(), 2);
//                    GroupCacheManager.updateChatGroupInfo(cacheGroup);
                }
            }
          /*  if (messageHelper.isOnline(t)) {
                ExitGroupNotifyRespBody exitGroupNotifyRespBody = new ExitGroupNotifyRespBody();
                exitGroupNotifyRespBody.setGroup(operationVO.getGroupId());
                UserDto notifyUser = UserDto.builder().userId(t).build();
                exitGroupNotifyRespBody.setUser(notifyUser);
                RespBody respBody = new RespBody(Command.COMMAND_EXIT_GROUP_NOTIFY_RESP, exitGroupNotifyRespBody);
                ImPacket imPacket = new ImPacket(Command.COMMAND_EXIT_GROUP_NOTIFY_RESP, respBody.toByte());
                List<ImChannelContext> userChanelContextList = JimServerAPI.getByUserId(t);
                userChanelContextList.forEach(chan -> {
                    try {
                        imServerConfig.getMessageHelper().getBindListener().onAfterGroupUnbind(chan, groupDto);
                        JimServerAPI.send(chan, imPacket);
                    } catch (ImException e) {
                        e.printStackTrace();
                    }
                });
                JimServerAPI.unbindGroup(t, operationVO.getGroupId());
            }*/
        });
        for (ChatGroupMemberVo item : operationVO.getMembers()) {
            JimServerAPI.unbindGroup(item.getUserId(), operationVO.getGroupId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void memberRemoveChatGroup(String groupId, List<String> removedUserIdList) {

        QueryWrapper<ChatGroup> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ChatGroup::getGroupId, groupId);
        ChatGroup chatGroup = getOne(queryWrapper);

        QueryWrapper<ChatGroupMember> chatGroupMemberQueryWrapper = new QueryWrapper<>();
        chatGroupMemberQueryWrapper.lambda().eq(ChatGroupMember::getGroupId, groupId);
        chatGroupMemberQueryWrapper.lambda().in(ChatGroupMember::getUserId, removedUserIdList);
        chatGroupMemberService.remove(chatGroupMemberQueryWrapper);

        //移除对应的会话
        conversationService.removeConversation(groupId, removedUserIdList);


        removedUserIdList.forEach(t -> {
            //如果是群组的话，则获取
            if (t.equals(chatGroup.getGroupOwner())) {
                ChatGroupMember chatGroupMember = chatGroupMemberService.getNewGroupName(groupId);
                if (chatGroupMember != null) {
                    chatGroupMember.setRoleType("owner");
                    chatGroupMemberService.updateById(chatGroupMember);
                    chatGroup.setGroupOwner(chatGroupMember.getUserId());
                    this.updateById(chatGroup);
//                    GroupDto cacheGroup = messageHelper.getGroupUsers(chatGroup.getGroupId(), 2);
//                    GroupCacheManager.updateChatGroupInfo(cacheGroup);
                }
            }
          /*  if (messageHelper.isOnline(t)) {
                ExitGroupNotifyRespBody exitGroupNotifyRespBody = new ExitGroupNotifyRespBody();
                exitGroupNotifyRespBody.setGroup(operationVO.getGroupId());
                UserDto notifyUser = UserDto.builder().userId(t).build();
                exitGroupNotifyRespBody.setUser(notifyUser);
                RespBody respBody = new RespBody(Command.COMMAND_EXIT_GROUP_NOTIFY_RESP, exitGroupNotifyRespBody);
                ImPacket imPacket = new ImPacket(Command.COMMAND_EXIT_GROUP_NOTIFY_RESP, respBody.toByte());
                List<ImChannelContext> userChanelContextList = JimServerAPI.getByUserId(t);
                userChanelContextList.forEach(chan -> {
                    try {
                        imServerConfig.getMessageHelper().getBindListener().onAfterGroupUnbind(chan, groupDto);
                        JimServerAPI.send(chan, imPacket);
                    } catch (ImException e) {
                        e.printStackTrace();
                    }
                });
                JimServerAPI.unbindGroup(t, operationVO.getGroupId());
            }*/
        });
        for (String removedUserId : removedUserIdList) {
            JimServerAPI.unbindGroup(removedUserId, groupId);
        }
    }

    @Override
    public ChatGroup getByGroupId(String groupId) {
        LambdaQueryWrapper queryWrapper = new QueryWrapper<ChatGroup>().lambda().eq(ChatGroup::getGroupId, groupId);
        return getOne(queryWrapper);
    }


 /*   private void saveUserAccount(List<ChatGroupMemberVo> chatGroupMemberVos) {
        chatGroupMemberVos.forEach(user -> {
            QueryWrapper<UserAccount> queryWrapper = new QueryWrapper();
            queryWrapper.lambda().eq(UserAccount::getUserId, user.getUserId());
            if (userAccountService.count(queryWrapper) == 0) {
                UserAccount userAccount = new UserAccount();
                userAccount.setUserNo(user.getUserNo());
                userAccount.setUserName(user.getUserName());
                userAccount.setUserId(user.getUserId());
                userAccountService.save(userAccount);
            }
        });
    }*/

   /* @Override
    public void saveGroupCache(List<ChatGroupMemberVo> groupMemberVos, ChatGroupDetailVo chatGroupDetailVo) {
        ImServerConfig imServerConfig = ImConfig.Global.get();
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        GroupDto groupDto = GroupDto.builder().build();
        BeanUtil.copyProperties(chatGroupDetailVo, groupDto);
        groupDto.setCreatedTime(chatGroupDetailVo.getCreatedTime());
        List<UserDto> userDtoList = new ArrayList<>();
        groupMemberVos.forEach(chatGroupMemberVo -> {
            UserDto userDto = UserDto.builder()
                    .userId(chatGroupMemberVo.getUserId())
                    .userNo(chatGroupMemberVo.getUserNo())
                    .userName(chatGroupMemberVo.getUserName())
                    .build();
            userDto.setStatus(messageHelper.isOnline(chatGroupMemberVo.getUserId()) ? UserStatusType.ONLINE.getStatus() : UserStatusType.ONLINE.getStatus());
            userDtoList.add(userDto);
        });
        groupDto.setUsers(userDtoList);
        userDtoList.forEach(t -> {
            if (messageHelper.isOnline(t.getUserId())) {
                List<ImChannelContext> userChanelContextList = JimServerAPI.getByUserId(t.getUserId());
                userChanelContextList.forEach(chan -> {
                    try {
                        imServerConfig.getMessageHelper().getBindListener().onAfterGroupBind(chan, groupDto);
                    } catch (ImException e) {
                        e.printStackTrace();
                    }
                });
            }
        });

        //保存
    }
*/

  /*  private void refreshUserGroup(ChatGroup chatGroup, List<ChatGroupMemberVo> chatGroupMembers) {
        GroupDto groupDto = GroupDto.builder().groupId(chatGroup.getGroupId()).build();
        BeanUtil.copyProperties(chatGroup, groupDto);
        groupDto.setCreatedTime(chatGroup.getCreatedTime().getTime());
        List<UserDto> userDtoList = new ArrayList<>();
        chatGroupMembers.forEach(item -> {
            UserDto userDto = UserDto.builder()
                    .userName(item.getUserName())
                    .userId(item.getUserId())
                    .userNo(item.getUserNo())
                    .build();
            userDtoList.add(userDto);
        });
        groupDto.setUsers(userDtoList);
        GroupCacheManager.updateChatGroupInfo(groupDto);
        List<String> userIdSet = chatGroupMembers.stream().map(ChatGroupMemberVo::getUserId).collect(Collectors.toList());
        GroupCacheManager.addUser(userIdSet, groupDto.getGroupId());
        JimServerAPI.bindUserListToGroup(userIdSet, groupDto.getGroupId());

    }*/

    private List<ChatGroupMember> mergeDeptUsers(List<ChatGroupMember> sourceMembers, String seid, List<String> deptIds, String groupId) {
        sourceMembers = sourceMembers.stream()
                .map(person -> {
                    person.setGroupId(groupId); // 设置对象的属性
                    return person;
                })
                .collect(Collectors.toList());

        if (ArrayUtil.isEmpty(deptIds)) {
            return sourceMembers;
        }
        List<ChatGroupMember> chatGroupMemberVos = userAccountService.getUserInfoByDeptIds(seid, deptIds);
        if (ArrayUtil.isEmpty(chatGroupMemberVos)) {
            return sourceMembers;
        }
        chatGroupMemberVos.stream()
                .map(person -> {
                    person.setGroupId(groupId); // 设置对象的属性
                    return person;
                })
                .collect(Collectors.toList());
        chatGroupMemberVos.addAll(sourceMembers);
        chatGroupMemberVos = chatGroupMemberVos.stream().filter(ChatGroupMember.distinctByKey(p -> p.getUserId()))
                .collect(Collectors.toList());

        return chatGroupMemberVos;
    }

  /*  private List<ChatGroupMemberVo> mergeDeptUsers(String seid, List<String> deptIds) {
        List<ChatGroupMemberVo> chatGroupMemberVos = userAccountService.getUserInfoByDeptIds(seid, deptIds);
        chatGroupMemberVos = chatGroupMemberVos.stream().filter(ChatGroupMemberVo.distinctByKey(p -> p.getUserId()))
                .collect(Collectors.toList());
        return chatGroupMemberVos;
    }*/


    /**
     * 校验建群条件，
     * 满足建群条件，返回true, 不满足返回false;
     *
     * @param members
     * @param chatType
     * @return
     */
    private Boolean chekCreateChatGroup(List<ChatGroupMember> members, Integer chatType) {
        //todo 如果只是某个人不能建群，是否直接建不了群
//        List<ChatGroupMember> chatGroupMemberVoList = mergeDeptUsers(members, seid, deptIds);
        if (CollectionUtils.isEmpty(members)) {
            throw new AppException(ImStatus.INVALID_VERIFICATION.getCode(), "会话创建失败");
        }

        if (ChatType.CHAT_TYPE_PUBLIC.getNumber() == chatType) {
            if (members.size() > 500) {
                throw new AppException(ImStatus.INVALID_VERIFICATION.getCode(), "群成员最多支持500人");
            }
        } else {
            if (members.size() > 2) {
                throw new AppException(ImStatus.INVALID_VERIFICATION.getCode(), "私聊人数不能大于2个人");
            }
        }
        return true;
    }

    /**
     * 组装私聊成员数据
     *
     * @return
     */
    private List<ChatGroupMember> constructPrivateMember(String groupId, String ownerUserId, List<ChatGroupMember> members) {
//        String groupId = chatGroupVo.getGroupId();
//        String ownerUserId = chatGroupVo.getOwner();
//        List<ChatGroupMemberVo> members = chatGroupVo.getMembers();
        ChatGroupMember ownerChatVo = members.stream().filter(member -> member.getUserId().equals(ownerUserId)).findAny().get();
        int ownnerIndex = members.indexOf(ownerChatVo);
        ChatGroupMember memberChatVo = members.get(1 - ownnerIndex);

        ChatGroupMember owner = new ChatGroupMember();
        ChatGroupMember member = new ChatGroupMember();


        owner.setGroupId(groupId);
        owner.setUserId(ownerChatVo.getUserId());
        owner.setUserName(ownerChatVo.getUserName());
        owner.setRoleType(MemberRoleType.OWNER.getCode());

        member.setGroupId(groupId);
        member.setUserId(memberChatVo.getUserId());
        member.setUserName(memberChatVo.getUserName());
        member.setRoleType(MemberRoleType.MEMBER.getCode());


        List<ChatGroupMember> chatGroupMembers = new ArrayList<>();
        chatGroupMembers.add(member);
        chatGroupMembers.add(owner);
        return chatGroupMembers;
    }


    @Override
    public void initBindUserGroup(String userId, List<String> groupIdList) {
        try {
            List<ImChannelContext> userChannelContext = JimServerAPI.getByUserId(userId);
            for (String groupId : groupIdList) {
                List<ImChannelContext> groupChannelContext = JimServerAPI.getByGroup(groupId);
                for (ImChannelContext singleUserChannelContext : userChannelContext) {
                    if (groupChannelContext.contains(singleUserChannelContext)) {
                        log.debug("用户已经的终端已经在群聊中，不再进行绑定:groupId:{} userId:{},terminal node:{}", groupId, singleUserChannelContext.getUserId(), singleUserChannelContext.getClientNode().toString());
                    } else {
                        JimServerAPI.bindGroup(singleUserChannelContext, groupId);
                    }
                }
            }
        } catch (Exception e) {
            log.error("登录完成后，设置用户所属group发生异常.userId:{}", userId, e);
        }
    }

  /*  @Override
    public ChatGroup getMsgNotify(PushMessage pushMessage, String lastMsgId) {
        QueryWrapper<ChatGroup> chatGroupQueryWrapper = new QueryWrapper<>();
        chatGroupQueryWrapper.eq("group_owner", pushMessage.getUserId());
        chatGroupQueryWrapper.eq("chat_type", ChatType.CHAT_TYPE_MSG_HELPER.getNumber());
        ChatGroup chatGroup;

        chatGroup = getOne(chatGroupQueryWrapper);
        if (chatGroup == null) {
            createMsgNotify(pushMessage, lastMsgId);
        }
      *//*  else {
            conversationService.updateLastMessage(chatGroup.getGroupId(), pushMessage.getUserId(), lastMsgId);
        }*//*
        chatGroup = getOne(chatGroupQueryWrapper);
        return chatGroup;
    }*/


    @Override
    @Transactional
    public ChatGroup createMsgHelperGroup(String receiver) {
        ChatGroup chatGroup = null;
        try {
            QueryWrapper<ChatGroup> chatGroupQueryWrapper = new QueryWrapper<>();
            chatGroupQueryWrapper.eq("group_owner", receiver);
            chatGroupQueryWrapper.eq("chat_type", ChatType.CHAT_TYPE_MSG_HELPER.getNumber());
            chatGroup = getOne(chatGroupQueryWrapper);
            if (chatGroup == null) {
                chatGroup = new ChatGroup();
                chatGroup.setGroupName("消息助手");
                chatGroup.setGroupId(UUIDUtil.getUUID())
                        .setGroupOwner(receiver)
                        .setChatType(ChatType.CHAT_TYPE_MSG_HELPER.getNumber())
                        .setCreatedBy(receiver)
                        .setStatus(ChatGroupStatus.NORMAL.getCode())
                        .setUpdatedBy(receiver)
                        .setCreatedTime(new Date())
                        .setUpdatedTime(new Date());
                save(chatGroup);
            } else {
                log.info("userId:{}  消息助手chatgroup 已存在", receiver);
            }
        } catch (Exception e) {
            log.error("初始化消息助手异常chatgroup ", e);
        }
        return chatGroup;
    }


    @Override
    @Transactional
    public void createMsgHelperGroupMember(ChatGroup chatGroup) {
        try {
            QueryWrapper<ChatGroupMember> chatGroupQueryWrapper = new QueryWrapper<>();
            chatGroupQueryWrapper.eq("group_id", chatGroup.getGroupId());
            ChatGroupMember chatGroupMember = chatGroupMemberService.getOne(chatGroupQueryWrapper);
            if (chatGroupMember == null) {
                chatGroupMember = new ChatGroupMember();
                chatGroupMember.setGroupId(chatGroup.getGroupId());
                chatGroupMember.setRoleType(MemberRoleType.getMemberRoleType(chatGroup.getGroupOwner(), chatGroup.getGroupOwner()).getCode());
                chatGroupMember.setUserId(chatGroup.getGroupOwner());
                chatGroupMember.setUserName(chatGroup.getGroupName());
                chatGroupMember.setCreatedBy(chatGroup.getGroupName());
                chatGroupMember.setUpdatedBy(chatGroup.getGroupName());
                chatGroupMember.setCreatedTime(new Date());
                chatGroupMember.setUpdatedTime(new Date());
                chatGroupMemberService.save(chatGroupMember);
            } else {
                log.info("userId:{}  消息助手 ChatGroupMember 已存在", JSONUtil.toJsonStr(chatGroup));
            }
        } catch (Exception e) {
            log.error("初始化消息助手member异常 ", e);
        }
    }

    @Override
    @Transactional
    public void createMsgHelperConversation(String receiver, String groupId) {
        try {
            QueryWrapper<Conversation> chatGroupQueryWrapper = new QueryWrapper<>();
            chatGroupQueryWrapper.eq("chatgroup_id", groupId);
            chatGroupQueryWrapper.eq("user_id", receiver);
            Conversation conversation = conversationService.getOne(chatGroupQueryWrapper);
            if (conversation == null) {
                conversation = new Conversation();
                conversation.setConversationId(UUIDUtil.getUUID())
                        .setConversationName("消息助手")
                        .setUserId(receiver)
                        .setGroupOwner(receiver)
                        .setChatgroupId(groupId)
                        .setChatType(ChatType.CHAT_TYPE_MSG_HELPER.getNumber())
                        .setUpdatedTime(new Date())
                        .setCreatedTime(new Date());
                conversationService.save(conversation);
            } else {
                log.info("receiver:{} groupId：{} 消息助手 会话 已存在", receiver, groupId);
            }
        } catch (Exception e) {
            log.error("初始化消息助手 conversation 异常 ", e);
        }
    }


 /*   @Deprecated
    public void createMsgNotify(PushMessage pushMessage, String lastMsgId) {
        ChatGroup chatGroup = new ChatGroup();
        chatGroup.setGroupName("消息助手");
        chatGroup.setGroupId(UUIDUtil.getUUID())
                .setGroupOwner(pushMessage.getUserId())
                .setChatType(ChatType.CHAT_TYPE_MSG_HELPER.getNumber())
                .setCreatedBy(pushMessage.getUserId())
                .setStatus(ChatGroupStatus.NORMAL.getCode())
                .setUpdatedBy(pushMessage.getUserId())
                .setCreatedTime(new Date())
                .setUpdatedTime(new Date());
        save(chatGroup);

        ChatGroupMember chatGroupMember = new ChatGroupMember();
        chatGroupMember.setGroupId(chatGroup.getGroupId());
        chatGroupMember.setRoleType(MemberRoleType.getMemberRoleType(chatGroup.getGroupOwner(), chatGroup.getGroupOwner()).getCode());
        chatGroupMember.setUserId(chatGroup.getGroupOwner());
        chatGroupMember.setUserName(chatGroup.getGroupName());
        chatGroupMember.setCreatedBy(chatGroup.getGroupName());
        chatGroupMember.setUpdatedBy(chatGroup.getGroupName());
        chatGroupMember.setCreatedTime(new Date());
        chatGroupMember.setUpdatedTime(new Date());
        chatGroupMemberService.save(chatGroupMember);

        Conversation conversation = new Conversation();
        conversation.setConversationId(UUIDUtil.getUUID())
                .setConversationName("消息助手")
                .setUserId(pushMessage.getUserId())
                .setGroupOwner(pushMessage.getUserId())
                .setChatgroupId(chatGroup.getGroupId())
                .setChatType(ChatType.CHAT_TYPE_MSG_HELPER.getNumber())
                .setLastMsgId(lastMsgId)
                .setUpdatedTime(new Date())
                .setCreatedTime(new Date());
        conversationService.save(conversation);

        String keyConversationIdList = CONVERSATION_ID_LIST_CACHE + pushMessage.getUserId();
        redisService.delKey(keyConversationIdList);
    }*/

    @Override
    public List<ChatGroupResp> getChatGroupResps(String userId, String searchKey) {
        List<ChatGroupResp> chatGroupResps = new ArrayList<>();
        List<Map<String, Object>> resultMap = new ArrayList<>();
        List<Map<String, Object>> resultMap1 = baseMapper.getGroupInclUserName(userId, searchKey);
        List<Map<String, Object>> resultMap2 = baseMapper.getGroupByName(userId, searchKey);
        List<Map<String, Object>> resultMap3 = baseMapper.getGroupLikeName(userId, searchKey);
        resultMap.addAll(resultMap1);
        resultMap.addAll(resultMap2);
        resultMap.addAll(resultMap3);
        for (Map<String, Object> map : resultMap) {
            ChatGroupResp chatGroupResp = new ChatGroupResp();
            chatGroupResp.setGroupId((String) map.get("groupId"));
            chatGroupResp.setGroupName((String) map.get("groupName"));
            chatGroupResp.setAvatar((String) map.get("avatar"));
            chatGroupResp.setSearchDesc((String) map.get("searchDesc"));
            chatGroupResps.add(chatGroupResp);
        }
        return chatGroupResps;
    }

}
