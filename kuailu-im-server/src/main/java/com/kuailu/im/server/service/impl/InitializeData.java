package com.kuailu.im.server.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.kuailu.im.core.packets.ChatType;
import com.kuailu.im.server.constant.IM_SERVER;
import com.kuailu.im.server.dto.GroupCacheDto;
import com.kuailu.im.server.enums.MemberRoleType;
import com.kuailu.im.server.enums.MessageTypeEnum;
import com.kuailu.im.server.model.entity.*;
import com.kuailu.im.server.service.*;
import com.kuailu.im.server.util.SpringContextHolder;
import com.kuailu.im.server.util.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 */
@Service
@Slf4j
public class InitializeData implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (contextRefreshedEvent.getApplicationContext().getParent() == null) {
            log.info("*******************************启动后只执行一次*******************************");
//            initConversation();
//            initFileHelper();
//            initTipMsg();
        }
    }

    /**
     * 这个版本之后就去掉
     */
    public void initConversation() {
        IUserAccountService userAccountService = SpringContextHolder.getBean(IUserAccountService.class);
        IConversationService conversationService = SpringContextHolder.getBean(IConversationService.class);
        IChatGroupMemberService groupMemberService = SpringContextHolder.getBean(IChatGroupMemberService.class);
        IChatGroupService groupService = SpringContextHolder.getBean(IChatGroupService.class);
        IChatMsgService msgService = SpringContextHolder.getBean(IChatMsgService.class);

        LambdaQueryWrapper<Conversation> queryWrapper = new QueryWrapper<Conversation>()
                .select("user_id")
                .groupBy("user_id")
                .lambda()
                .ne(Conversation::getChatType, ChatType.CHAT_TYPE_MSG_HELPER.getNumber())
                .ne(Conversation::getChatType, ChatType.FILE_HELPER.getNumber())
                .isNotNull(Conversation::getUserId);
        List<Conversation> allUserIdHasConversation = conversationService.list(queryWrapper);

        List<UserAccount> userAccountList = userAccountService.list();
        List<UserAccount> userToNewConversation = new ArrayList<>();

        List<String> userIds = allUserIdHasConversation.stream().map(Conversation::getUserId).collect(Collectors.toList());
        for (UserAccount userAccount : userAccountList) {
            String userId = userAccount.getUserId();
            List<String> groupIds = getCommonConversation(userId);
            if (CollectionUtils.isNotEmpty(groupIds) && !userIds.contains(userId)) {
                //必须是有普通会话数据，并且没有生成会话，才需要生成
                userToNewConversation.add(userAccount);
            }
        }

        if (CollectionUtils.isEmpty(userToNewConversation)) {
            log.info("所有人都已经初始化会话消息");
            return;
        }

        for (UserAccount userAccount : userToNewConversation) {
            log.info("用户：{} userId:{} 需要生产会话", userAccount.getUserName(), userAccount.getUserId());
        }


        for (UserAccount userAccount : userToNewConversation) {
            String userId = userAccount.getUserId();
            List<Conversation> conversationList = new ArrayList<>();

            List<String> groupIds = getCommonConversation(userId);
           /* if (CollectionUtils.isEmpty(groupIds)) {
                log.info("用户：{},{}   没有普通会话数据", userAccount.getUserName(), userId);
                continue;
            }*/
            conversationList = conversationService.list(new QueryWrapper<Conversation>().orderByDesc("created_time").lambda().isNull(Conversation::getUserId)  //旧数据，userId 肯定是空的
                    .in(Conversation::getChatgroupId, groupIds));
            List<String> conversationIdNotShown = new ArrayList<>();

            List<String> groupIdListPrivateGroup = conversationList.stream().filter(conversation -> conversation.getChatType() == ChatType.CHAT_TYPE_PRIVATE.getNumber()).map(Conversation::getChatgroupId).collect(Collectors.toList());
            Map<String, ChatGroupMember> groupIdToMember = new HashMap<>();
            if (CollectionUtils.isNotEmpty(groupIdListPrivateGroup)) {
                groupIdToMember = groupMemberService.getPrivateChatterMember(groupIdListPrivateGroup, userId);
            }

            for (String groupId : groupIds) {
                ChatGroup chatGroup = groupService.getByGroupId(groupId);
                Integer chatType = chatGroup.getChatType();

                LambdaQueryWrapper<Conversation> queryWrapperConversation = new QueryWrapper<Conversation>().last("limit 1").lambda().eq(Conversation::getChatgroupId, groupId);
                Conversation existConversation = conversationService.getOne(queryWrapperConversation);
                Conversation newConversation = new Conversation();
                String conversationId = null;

                if (null == existConversation) {
                    log.error("groupId:{} userId:{} 对应的会话已废弃", groupId, userId);
                    conversationId = UUIDUtil.getUUID();
                    newConversation.setCreatedTime(chatGroup.getCreatedTime());
                    newConversation.setUpdatedTime(chatGroup.getUpdatedTime());
                } else {
                    newConversation.setCreatedTime(existConversation.getCreatedTime());
                    newConversation.setUpdatedTime(existConversation.getUpdatedTime());
                    conversationId = existConversation.getConversationId();
                    chatType = existConversation.getChatType();
                }

                Boolean isSelfChat = groupMemberService.isSelfChat(groupId);
                String roleType = groupMemberService.getRoleTypeInPrivateChat(userId, groupId);
                if (!isSelfChat && MemberRoleType.MEMBER.getCode().equals(roleType)) {
                    if (msgService.count(new QueryWrapper<ChatMsg>().lambda().eq(ChatMsg::getGroupId, groupId)) <= 0) {
                        conversationIdNotShown.add(conversationId);
                    }
                }
                //保存为新数据

                newConversation.setConversationId(conversationId);
                newConversation.setChatgroupId(groupId);
                newConversation.setUserId(userId);
                newConversation.setChatType(chatType);

                int selfChat = (isSelfChat ? 1 : 0);
                newConversation.setSelfChat(selfChat);
                newConversation.setRoleType(roleType);

                if (ChatType.CHAT_TYPE_PRIVATE.getNumber() == chatType) {
                    newConversation.setConversationName(groupIdToMember.getOrDefault(groupId, new ChatGroupMember()).getUserName());
                    newConversation.setReceiver(groupIdToMember.getOrDefault(groupId, new ChatGroupMember()).getUserId());
                    ChatGroupMember oppositeMember = groupMemberService.getPrivateChatter(userId, groupId);
                    newConversation.setAvatar(userAccountService.getAvatarUrl(oppositeMember.getUserId()));
                } else if (ChatType.CHAT_TYPE_PUBLIC.getNumber() == chatType) {
                    newConversation.setConversationName(chatGroup.getGroupName());
                    newConversation.setReceiver(groupId);
                    newConversation.setAvatar(chatGroup.getAvatar());
                }
                newConversation.setGroupOwner(chatGroup.getGroupOwner());
                UpdateWrapper updateWrapper = new UpdateWrapper<WhiteListMember>().eq("user_id", userId).eq("conversation_id", conversationId);
                conversationService.saveOrUpdate(newConversation, updateWrapper);
            }

        }
    }


    public List<String> getCommonConversation(String userId) {
        IChatGroupMemberService groupMemberService = SpringContextHolder.getBean(IChatGroupMemberService.class);
        IChatGroupService groupService = SpringContextHolder.getBean(IChatGroupService.class);
        List<String> groupIdList = new ArrayList<>();
        LambdaQueryWrapper queryWrapper = new QueryWrapper<ChatGroupMember>()
                .select("group_id").lambda()
                .eq(ChatGroupMember::getUserId, userId)
                .groupBy(ChatGroupMember::getGroupId);
        List<ChatGroupMember> groupMemberList = groupMemberService.list(queryWrapper);
        if (CollectionUtils.isNotEmpty(groupMemberList)) {
            List<String> groupMemberIdList = groupMemberList.stream().map(ChatGroupMember::getGroupId).collect(Collectors.toList());
            LambdaQueryWrapper<ChatGroup> queryGroupIdWrapper = new QueryWrapper<ChatGroup>()
                    .lambda()
                    .in(ChatGroup::getGroupId, groupMemberIdList)
                    .ne(ChatGroup::getChatType, ChatType.FILE_HELPER.getNumber())
                    .ne(ChatGroup::getChatType, ChatType.CHAT_TYPE_MSG_HELPER.getNumber());
            List<ChatGroup> chatGroupList = groupService.list(queryGroupIdWrapper);
            if (CollectionUtils.isNotEmpty(chatGroupList)) {
                groupIdList = chatGroupList.stream().map(ChatGroup::getGroupId).collect(Collectors.toList());
            }
        }
        return groupIdList;
    }


 /*   private List<String> getCommonUserId() {
        List<String> userIdList = new ArrayList<>();
        IChatGroupMemberService groupMemberService = SpringContextHolder.getBean(IChatGroupMemberService.class);
        IChatGroupService groupService = SpringContextHolder.getBean(IChatGroupService.class);

        LambdaQueryWrapper<ChatGroup> queryGroupIdWrapper = new QueryWrapper<ChatGroup>()
                .select("group_id")
                .lambda()
                .or()
                .ne(ChatGroup::getChatType, ChatType.FILE_HELPER.getNumber())
                .ne(ChatGroup::getChatType, ChatType.CHAT_TYPE_MSG_HELPER.getNumber());


        List<ChatGroup> chatGroupList = groupService.list(queryGroupIdWrapper);
        if (CollectionUtils.isNotEmpty(chatGroupList)) {
            List<String> groupIdList = chatGroupList.stream().map(ChatGroup::getGroupId).collect(Collectors.toList());
            LambdaQueryWrapper<ChatGroupMember> queryGroupMemberWrapper = new QueryWrapper<ChatGroupMember>()
                    .select("user_id")
                    .groupBy("user_id")
                    .lambda()
                    .in(ChatGroupMember::getGroupId, groupIdList);

            List<ChatGroupMember> groupMemberList = groupMemberService.list(queryGroupMemberWrapper);
            if (CollectionUtils.isNotEmpty(groupMemberList)) {
                userIdList = groupMemberList.stream().map(ChatGroupMember::getUserId).collect(Collectors.toList());
            }
        }
        return userIdList;
    }
*/

    public void initFileHelper() {
        IUserAccountService userAccountService = SpringContextHolder.getBean(IUserAccountService.class);
        IConversationService conversationService = SpringContextHolder.getBean(IConversationService.class);

        List<String> userNotHashFileHelperList = new ArrayList<>();
        List<String> userHasFileHelper = new ArrayList<>();

        LambdaQueryWrapper<Conversation> queryWrapper = new QueryWrapper<Conversation>()
                .select("user_id")
                .lambda()
                .eq(Conversation::getChatType, ChatType.FILE_HELPER.getNumber())
                .ne(Conversation::getUserId, IM_SERVER.USER_ID);

        List<Conversation> allUserHasFileHelper = conversationService.list(queryWrapper);
        if (CollectionUtils.isEmpty(allUserHasFileHelper)) {
            log.info("所有人都没有创建文件助手");
            userNotHashFileHelperList = userAccountService.list().stream().map(UserAccount::getUserId).collect(Collectors.toList());
        } else {
            userHasFileHelper = allUserHasFileHelper.stream().map(Conversation::getUserId).collect(Collectors.toList());
            LambdaQueryWrapper<UserAccount> queryWrapperNotHasFileHelper = new QueryWrapper<UserAccount>()
                    .lambda()
                    .notIn(UserAccount::getUserId, userHasFileHelper);
            userNotHashFileHelperList = userAccountService.list(queryWrapperNotHasFileHelper).stream().map(UserAccount::getUserId).collect(Collectors.toList());
        }


        if (CollectionUtils.isEmpty(userNotHashFileHelperList)) {
            log.info("所有人已经有文件助手");
            return;
        }

        for (String userId : userHasFileHelper) {
            log.info("用户 userId:{} 已经有创建文件助手 ", userId);
            LambdaQueryWrapper<Conversation> queryWrapperFileHelper = new QueryWrapper<Conversation>()
                    .lambda().eq(Conversation::getUserId, userId)
                    .eq(Conversation::getChatType, ChatType.FILE_HELPER.getNumber());
            List<Conversation> conversationList = conversationService.list(queryWrapperFileHelper);
            if (conversationList.size() == 1) {
                if (!conversationList.get(0).getConversationName().equals(ChatType.FILE_HELPER.getName())) {
                    //名称不对
                    cleanFileHelper(conversationList.stream().map(Conversation::getChatgroupId).collect(Collectors.toList()));
                    userNotHashFileHelperList.add(userId);
                }

            } else {
                //数量不对
                cleanFileHelper(conversationList.stream().map(Conversation::getChatgroupId).collect(Collectors.toList()));
                userNotHashFileHelperList.add(userId);
            }
        }

        for (String userId : userNotHashFileHelperList) {
            log.info("用户：{}  没有创建文件助手,需要创建", userId);
        }

        for (String userId : userNotHashFileHelperList) {
            conversationService.createFileHelper(userId);
            log.info("用户 userID:{}   创建文件助手成功", userId);
        }
        conversationService.cleanUserConversationIdCache(userNotHashFileHelperList);
    }


    public void cleanFileHelper(List<String> groupIdList) {
        if (CollectionUtils.isEmpty(groupIdList)) {
            return;
        }
        IChatGroupService groupService = SpringContextHolder.getBean(IChatGroupService.class);
        IConversationService conversationService = SpringContextHolder.getBean(IConversationService.class);
        IChatGroupMemberService groupMemberService = SpringContextHolder.getBean(IChatGroupMemberService.class);

        LambdaUpdateWrapper<Conversation> removeConversation = new UpdateWrapper<Conversation>()
                .lambda()
                .in(Conversation::getChatgroupId, groupIdList)
                .eq(Conversation::getChatType, ChatType.FILE_HELPER.getNumber());
        conversationService.remove(removeConversation);

        LambdaUpdateWrapper<ChatGroup> removeGroupWrapper = new UpdateWrapper<ChatGroup>()
                .lambda()
                .in(ChatGroup::getGroupId, groupIdList);
        groupService.remove(removeGroupWrapper);

        LambdaUpdateWrapper<ChatGroupMember> removeGroupMemberWrapper = new UpdateWrapper<ChatGroupMember>()
                .lambda()
                .in(ChatGroupMember::getGroupId, groupIdList);
        groupMemberService.remove(removeGroupMemberWrapper);
    }


    public void initTipMsg() {
        IChatMsgService msgService = SpringContextHolder.getBean(IChatMsgService.class);
        IUserAccountService userAccountService = SpringContextHolder.getBean(IUserAccountService.class);
        IConversationService conversationService = SpringContextHolder.getBean(IConversationService.class);
        List<UserAccount> userAccountList = new ArrayList<>();

        LambdaQueryWrapper<ChatMsg> queryWrapper = new QueryWrapper<ChatMsg>()
                .select("receiver")
                .groupBy("receiver").lambda().eq(ChatMsg::getMsgType, MessageTypeEnum.TIPS.getCode());
        List<ChatMsg> chatMsgList = msgService.list(queryWrapper);

        if (CollectionUtils.isEmpty(chatMsgList)) {
            log.info("所有人都没有初始化提示消息");
            userAccountList = userAccountService.list();
        } else {
            List<String> userIds = chatMsgList.stream().map(ChatMsg::getReceiver).collect(Collectors.toList());
            LambdaQueryWrapper<UserAccount> queryWrapperNotHasFileHelper = new QueryWrapper<UserAccount>()
                    .lambda()
                    .notIn(UserAccount::getUserId, userIds);
            userAccountList = userAccountService.list(queryWrapperNotHasFileHelper);
        }
        if (CollectionUtils.isEmpty(userAccountList)) {
            log.info("所有人已经 有提示消息");
            return;
        }

        for (UserAccount userAccount : userAccountList) {
            log.info("用户：{}  没有创建提示消息,需要创建", userAccount);
        }


        QueryWrapper<ChatMsg> queryTipMsgWrapper = new QueryWrapper<ChatMsg>()
                .eq("msg_type", MessageTypeEnum.TIPS.getCode());
        for (UserAccount userAccount : userAccountList) {
            String userId = userAccount.getUserId();
            queryTipMsgWrapper.eq("receiver", userId);
            try {
                if (msgService.count(queryTipMsgWrapper) <= 0) {
                    //初始化提示消息
                    Conversation fileHelperConversation = conversationService.getFileHelperByUserId(userId);
                    ChatMsg chatMsg = new ChatMsg.TipBuilder(fileHelperConversation.getConversationId(), userId, fileHelperConversation.getChatgroupId()).build();
                    msgService.save(chatMsg);

                    conversationService.cleanUserConversationIdCache(userId);
                    conversationService.cleanContentCache(userId,fileHelperConversation.getConversationId());
                }
            } catch (Exception e) {
                log.error("用户：{} 创建提示消息失败", userId, e);
            }

        }
    }

}
