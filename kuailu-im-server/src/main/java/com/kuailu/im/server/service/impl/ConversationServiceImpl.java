package com.kuailu.im.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.google.common.collect.Lists;
import com.kuailu.im.core.ImPacket;
import com.kuailu.im.core.common.PublicRedisKey;
import com.kuailu.im.core.packets.ChatType;
import com.kuailu.im.core.packets.Command;
import com.kuailu.im.core.packets.RespBody;
import com.kuailu.im.core.utils.HttpUtil;
import com.kuailu.im.server.JimServerAPI;
import com.kuailu.im.server.constant.IM_SERVER;
import com.kuailu.im.server.dto.ConversationCacheDto;
import com.kuailu.im.server.dto.GroupCacheDto;
import com.kuailu.im.server.enums.MemberRoleType;
import com.kuailu.im.server.enums.YesOrNoEnum;
import com.kuailu.im.server.model.entity.*;
import com.kuailu.im.server.mapper.ConversationMapper;
import com.kuailu.im.server.response.ConversationDto;
import com.kuailu.im.server.response.MessageHistoryResponse;
import com.kuailu.im.server.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kuailu.im.server.util.PassService;
import com.kuailu.im.server.util.RedisService;
import com.kuailu.im.server.util.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.kuailu.im.core.packets.Command.COMMAND_JOIN_GROUP_NOTIFY_RESP;

@Service
@Slf4j
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements IConversationService {

    private final String CONVERSATION_ID_LIST_CACHE = "CONVERSATION_ID_LIST_CACHE_";
    private final String CONVERSATION_CONTENT_CACHE = "CONVERSATION_CONTENT_CACHE_";

    @Autowired
    IChatGroupService groupService;

    @Autowired(required = false)
    @Lazy
    IChatMsgService msgService;


    @Autowired
    IAtMsgService atMsgService;

    @Autowired
    RedisService redisService;

    @Autowired
    IChatGroupMemberService groupMemberService;

    @Autowired(required = false)
    @Lazy
    IUserAccountService userAccountService;

    @Value("${kuailu.apiUrl}")
    String kuailuApiUrl;


    @Autowired
    PassService passService;


    /**
     * 查询当前用户的会话
     *
     * @return
     */
    @Override
    public List<ConversationCacheDto> getCurrentUserConversations(List<String> groupIds, String userId, String seid) {
        List<ConversationCacheDto> conversationCacheDtoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(groupIds)) {
//            List<String> conversationIdList = getConversationIdByGroupId(groupIds, userId);
            List<String> conversationIdList = getConversationIdUserId(groupIds, userId);
            for (String conversationId : conversationIdList) {
                ConversationCacheDto conversationCacheDto = getContentByConversationId(userId, conversationId, seid);
                conversationCacheDtoList.add(conversationCacheDto);
            }
        }
        return conversationCacheDtoList;
    }


    @Override
    public List<String> getConversationIdUserId(List<String> groupIds, String userId) {
        String key = CONVERSATION_ID_LIST_CACHE + userId;
        List<String> conversationIdList = redisService.range(key, String.class);

        if (CollectionUtils.isEmpty(conversationIdList)) {
            List<Conversation> conversationList = new ArrayList<>();

            /*************************** 先根据userId 去查  *******************************/
            LambdaQueryWrapper queryWrapperByUserId = new QueryWrapper<Conversation>().orderByDesc("created_time").select("conversation_id", "chat_type", "chatgroup_id").lambda()
//                    .ne(Conversation::getChatType, ChatType.FILE_HELPER.getNumber())
                    .eq(Conversation::getUserId, userId);
            conversationList = list(queryWrapperByUserId);
            if (CollectionUtils.isNotEmpty(conversationList)) {
                conversationIdList = conversationList.stream().map(Conversation::getConversationId).collect(Collectors.toList());
                List<String> idNotShown = conversationList.stream().filter(conversation -> MemberRoleType.MEMBER.getCode().equals(conversation.getRoleType()) && YesOrNoEnum.NO.getCode() == conversation.getSelfChat() && ChatType.CHAT_TYPE_PRIVATE.getNumber() == conversation.getChatType()).map(Conversation::getConversationId).collect(Collectors.toList());
                for (String conversationIdToRemove : idNotShown) {
                    LambdaQueryWrapper queryWrapper = new QueryWrapper<ChatMsg>().lambda().eq(ChatMsg::getConversationId, conversationIdToRemove);
                    if (msgService.count(queryWrapper) <= 0) {
                        conversationIdList.remove(conversationIdToRemove);
                    }
                }
            }
            /*************************** 先根据userId 去查  *******************************/

            /*************************** todo 这一段主要为了兼容老数据，后续版本可以删掉 *******************************/
            if (CollectionUtils.isEmpty(conversationList)) {
                conversationList = list(new QueryWrapper<Conversation>().orderByDesc("created_time").lambda().isNull(Conversation::getUserId)  //旧数据，userId 肯定是空的
                        .in(Conversation::getChatgroupId, groupIds));
                List<String> conversationIdNotShown = new ArrayList<>();

                List<String> groupIdListPrivateGroup = conversationList.stream().filter(conversation -> conversation.getChatType() == ChatType.CHAT_TYPE_PRIVATE.getNumber()).map(Conversation::getChatgroupId).collect(Collectors.toList());
                Map<String, ChatGroupMember> groupIdToMember = new HashMap<>();
                if (CollectionUtils.isNotEmpty(groupIdListPrivateGroup)) {
                    groupIdToMember = groupMemberService.getPrivateChatterMember(groupIdListPrivateGroup, userId);
                }

                for (String groupId : groupIds) {
//                    String groupId = conversation.getChatgroupId();

                    LambdaQueryWrapper queryWrapperConversation = new QueryWrapper<Conversation>().last("limit 1").lambda().eq(Conversation::getChatgroupId, groupId);
                    Conversation existConversation = getOne(queryWrapperConversation);
                    if (null == existConversation) {
                        log.error("groupId:{} userId:{} 对应的会话已废弃", groupId, userId);
                        continue;
                    }

                    String conversationId = existConversation.getConversationId();
                    Integer chatType = existConversation.getChatType();
                    Boolean isSelfChat = groupMemberService.isSelfChat(groupId);
                    String roleType = groupMemberService.getRoleTypeInPrivateChat(userId, groupId);
                    if (!isSelfChat && MemberRoleType.MEMBER.getCode().equals(roleType)) {
                        LambdaQueryWrapper queryWrapper = new QueryWrapper<ChatMsg>().lambda().eq(ChatMsg::getGroupId, groupId);
                        if (msgService.count(queryWrapper) <= 0) {
                            conversationIdNotShown.add(conversationId);
                        }
                    }

                    //保存为新数据
                    Conversation newConversation = new Conversation();
                    newConversation.setConversationId(conversationId);
                    newConversation.setChatgroupId(groupId);
                    newConversation.setUserId(userId);
                    newConversation.setChatType(chatType);
//                    newConversation.setLastMsgId(existConversation.getLastMsgId());
//                    newConversation.setNoDisturb(existConversation.getNoDisturb());

                    int selfChat = (isSelfChat ? 1 : 0);
                    newConversation.setSelfChat(selfChat);
                    newConversation.setRoleType(roleType);
//                    newConversation.setUnreadCount(existConversation.getUnreadCount());
                    newConversation.setCreatedTime(existConversation.getCreatedTime());
                    newConversation.setUpdatedTime(existConversation.getUpdatedTime());

                    ChatGroup chatGroup = groupService.getOne(new QueryWrapper<ChatGroup>().lambda().eq(ChatGroup::getGroupId, groupId));
                    if (ChatType.CHAT_TYPE_PRIVATE.getNumber() == chatType) {
                        newConversation.setConversationName(groupIdToMember.getOrDefault(groupId, new ChatGroupMember()).getUserName());
                        newConversation.setReceiver(groupIdToMember.getOrDefault(groupId, new ChatGroupMember()).getUserId());
                        ChatGroupMember oppositeMember = groupMemberService.getPrivateChatter(userId, groupId);
                        newConversation.setAvatar(userAccountService.getAvatarUrl(oppositeMember.getUserId()));
                    } else if (ChatType.CHAT_TYPE_PUBLIC.getNumber() == chatType) {
                        newConversation.setConversationName(chatGroup.getGroupName());
                        newConversation.setReceiver(groupId);
                        newConversation.setAvatar(chatGroup.getAvatar());
                    } else if (ChatType.FILE_HELPER.getNumber() == chatType && (!fileHelperExist(userId))) {
                        newConversation.setConversationName(ChatType.FILE_HELPER.getName());
                        newConversation.setReceiver(IM_SERVER.USER_ID);
                    }
                    newConversation.setGroupOwner(chatGroup.getGroupOwner());
                    UpdateWrapper updateWrapper = new UpdateWrapper<WhiteListMember>().eq("user_id", userId).eq("conversation_id", conversationId);
                    saveOrUpdate(newConversation, updateWrapper);
                }
                conversationIdList = conversationList.stream().map(Conversation::getConversationId).collect(Collectors.toList());
                conversationIdList.removeAll(conversationIdNotShown);
            }
            /*************************** todo 这一段主要为了兼容老数据，后续版本可以删掉 *******************************/
            redisService.addZSet(key, conversationIdList);
        }
        return conversationIdList;
    }


    /**
     * 查询当前用户的会话
     *
     * @return
     */
    @Override
    @Deprecated
    public List<String> getConversationIdByGroupId(List<String> groupIds, String userId) {
        String key = CONVERSATION_ID_LIST_CACHE + userId;
        List<String> conversationIdList = redisService.range(key, String.class);

        if (CollectionUtils.isEmpty(conversationIdList)) {
            List<Conversation> conversationList = list(new QueryWrapper<Conversation>().select("conversation_id", "chat_type", "chatgroup_id").orderByDesc("created_time").lambda().in(Conversation::getChatgroupId, groupIds));
            conversationIdList = conversationList.stream().map(Conversation::getConversationId).collect(Collectors.toList());

            /***************************  从来没有发过消息的私聊，是不用展示出来的   ************/
            List<String> conversationIdNotShown = new ArrayList<>();
            for (Conversation conversation : conversationList) {
                String chatGroupId = conversation.getChatgroupId();
                Boolean isSelfChat = groupMemberService.isSelfChat(chatGroupId);
                String roleType = groupMemberService.getRoleTypeInPrivateChat(userId, chatGroupId);
                if (!isSelfChat && MemberRoleType.MEMBER.getCode().equals(roleType)) {
                    LambdaQueryWrapper queryWrapper = new QueryWrapper<ChatMsg>().lambda().eq(ChatMsg::getGroupId, chatGroupId);
                    if (msgService.count(queryWrapper) <= 0) {
                        conversationIdNotShown.add(conversation.getConversationId());
                    }
                }
            }
            conversationIdList.removeAll(conversationIdNotShown);
            /***************************  从来没有发过消息的私聊，是不用展示出来的   ************/
            redisService.addZSet(key, conversationIdList);
        }
        return conversationIdList;
    }

    @Override
    public Conversation getFileHelperByUserId(String userId) {
        LambdaQueryWrapper queryWrapper = new QueryWrapper<Conversation>().lambda().eq(Conversation::getUserId, userId).eq(Conversation::getChatType, ChatType.FILE_HELPER.getNumber());
        return getOne(queryWrapper);
    }

    @Override
    public String getReceiver(String userId, String conversationId) {
        String key = CONVERSATION_CONTENT_CACHE + conversationId + "_" + userId;
        String receiver = redisService.getHashValue(key, "receiver", String.class);
        if (StringUtils.isEmpty(receiver)) {
            ConversationCacheDto conversationCacheDto = getContentByConversationId(userId, conversationId, "");
            receiver = conversationCacheDto.getReceiver();
        }
        return receiver;
    }


    private ConversationCacheDto getContentByConversationId(String userId, String conversationId, String seid) {
        String key = getContentCacheKey(conversationId, userId);
        String conversationIdCache = redisService.getHashValue(key, "conversationId", String.class);

        ConversationCacheDto conversationCacheDto = null;
        try {
            if (StringUtils.isEmpty(conversationIdCache)) {
                Conversation conversation = null;
                conversation = getOne(new QueryWrapper<Conversation>().select("chatgroup_id", "avatar", "group_owner", "receiver", "conversation_name", "last_msg_id", "chat_type", "no_disturb", "created_time").lambda().eq(Conversation::getUserId, userId).eq(Conversation::getConversationId, conversationId));
                conversationCacheDto = new ConversationCacheDto();
                conversationCacheDto.setConversationId(conversationId);
                conversationCacheDto.setUserId(userId);
                //todo 后续要优化掉，用数据库直接保存的值，并适时更新对方的头像地址
                if (ChatType.CHAT_TYPE_PRIVATE.getNumber() == conversation.getChatType()) {
                    conversationCacheDto.setAvatar(userAccountService.getAvatarUrl(conversation.getReceiver()));
                } else {
                    conversationCacheDto.setAvatar(conversation.getAvatar());
                }
                conversationCacheDto.setConversationName(conversation.getConversationName());
                conversationCacheDto.setGroupId(conversation.getChatgroupId());
                conversationCacheDto.setGroupOwner(conversation.getGroupOwner());
                conversationCacheDto.setCreatedTime(conversation.getCreatedTime().getTime());
                conversationCacheDto.setReceiver(conversation.getReceiver());
                conversationCacheDto.setChatType(conversation.getChatType());
                conversationCacheDto.setDisturbType(conversation.getNoDisturb());
                redisService.putHashAll(key, BeanUtil.beanToMap(conversationCacheDto, false, true), 10, TimeUnit.MINUTES);
            } else {
                conversationCacheDto = redisService.getHashValue(key, ConversationCacheDto.class);
            }
            String groupId = conversationCacheDto.getGroupId();

            conversationCacheDto.setLastAtAvatar(getLastAtAvatar(key, groupId, userId));
            conversationCacheDto.setLastMsg(getLastMsgId(key, groupId, userId));
            conversationCacheDto.setUnReaderMsgCount(getUnReadMsgCount(key, groupId, userId, conversationCacheDto.getChatType(), seid));
        } catch (Exception e) {
            log.error("获取会话内容失败 conversationId :{} userId:{}", conversationId, userId, e);
        }

        return conversationCacheDto;
    }

    private String getLastAtAvatar(String key, String groupId, String userId) {
        String lastAtAvatar = redisService.getHashValue(key, "lastAtAvatar", String.class);
        if (!redisService.hasHashKey(key, "lastAtAvatar")) {
            MessageHistoryResponse.HistoryChat lastUnreadAtMsg = atMsgService.getLastUnReadMsg(groupId, userId);
            lastAtAvatar = userAccountService.getAvatarUrl(lastUnreadAtMsg.getSender());
            redisService.putHash(key, "lastAtAvatar", lastAtAvatar);
        }
        return lastAtAvatar;
    }

    private String getContentCacheKey(String conversationId, String userId) {
        return CONVERSATION_CONTENT_CACHE + conversationId + "_" + userId;
    }

    private ChatMsg getLastMsgId(String key, String groupId, String userId) {
        ChatMsg lastMsg = redisService.getHashValue(key, "lastMsg", ChatMsg.class);
        if (!redisService.hasHashKey(key, "lastMsg")) {
            lastMsg = msgService.getLastMsg(groupId);
            redisService.putHash(key, "lastMsg", lastMsg, 10, TimeUnit.MINUTES);
        }
        return lastMsg;
    }

    private Integer getUnReadMsgCount(String key, String groupId, String userId, Integer chatType, String seid) {
        if (ChatType.CHAT_TYPE_MSG_HELPER.getNumber() == chatType) {
            return passService.getMsgHelperUnReadCount(seid);
        }

        Integer unreadCount = redisService.getHashValue(key, "unReaderMsgCount", Integer.class);
        if (!redisService.hasHashKey(key, "unReaderMsgCount")) {
            if (ChatType.CHAT_TYPE_PUBLIC.getNumber() == chatType) {
                unreadCount = msgService.getGroupUnreadCount(groupId, userId);
            } else {
                //todo 文件助手的时候，貌似有问题。receiver不应该是当前用户？？
                unreadCount = msgService.getPrivateUnreadCountByGroupId(groupId, userId);
            }
            redisService.putHash(key, "unReaderMsgCount", unreadCount, 10, TimeUnit.MINUTES);
        }
        return unreadCount;
    }

    /**
     * 某一个会话的未读数
     *
     * @param groupId
     * @param userId
     * @param chatType
     * @return
     */
    private Integer getUnReadMsgCount(String groupId, String userId, Integer chatType) {
        Integer unreadCount = 0;
        if (ChatType.CHAT_TYPE_PUBLIC.getNumber() == chatType) {
            unreadCount = msgService.getGroupUnreadCount(groupId, userId);
        } else if (ChatType.CHAT_TYPE_PRIVATE.getNumber() == chatType || ChatType.FILE_HELPER.getNumber() == chatType) {
            //todo 文件助手的时候，貌似有问题。receiver不应该是当前用户？？
            unreadCount = msgService.getPrivateUnreadCountByGroupId(groupId, userId);
        }
        return unreadCount;
    }


    @Override
    public Integer getTotalUnReadMsgCount(String currentUserId) {
        List<GroupCacheDto> groupCacheDtoList = groupMemberService.getUserGroups(currentUserId);
        List<String> groupIdList = groupCacheDtoList.stream().map(GroupCacheDto::getGroupId).collect(Collectors.toList());
        List<ConversationCacheDto> conversationCacheDtoList = getCurrentUserConversations(groupIdList, currentUserId, null);
        Integer umReadCount = conversationCacheDtoList.stream()
                .filter(item -> null != item.getUnReaderMsgCount() && ChatType.CHAT_TYPE_MSG_HELPER.getNumber() != item.getChatType())
                .mapToInt(ConversationCacheDto::getUnReaderMsgCount).sum();
        return umReadCount;
    }

    @Override
    public void sendConversation(String conversationName, String groupId, String conversationId, Long createdTime, Integer chatType, String owner, String avatar) {
        try {
            ConversationDto conversationDto = new ConversationDto();
            conversationDto.setGroupId(groupId);
            conversationDto.setConversationId(conversationId);
            conversationDto.setConversationName(conversationName);
            conversationDto.setCreateTime(createdTime);
            conversationDto.setChatType(chatType);
            conversationDto.setGroupOwner(owner);

            if (ChatType.CHAT_TYPE_PUBLIC.getNumber() == chatType) {
                conversationDto.setReceiver(groupId);
                conversationDto.setAvatar(avatar);
                ImPacket conversationResp = new ImPacket(Command.COMMAND_JOIN_GROUP_NOTIFY_RESP, new RespBody(COMMAND_JOIN_GROUP_NOTIFY_RESP, conversationDto).toByte());
                JimServerAPI.sendToGroup(groupId, conversationResp);
            }
        } catch (Exception e) {
            log.error("推送建群成功失败", e);
        }
    }

    @Override
    public void updateLastMessage(String groupId, String userId, String lastMsgId) {
        LambdaUpdateWrapper updateWrapper = new UpdateWrapper<Conversation>().set("last_msg_id", lastMsgId).lambda()
                .eq(Conversation::getChatgroupId, groupId)
                .eq(Conversation::getUserId, userId);
        update(updateWrapper);
    }

    public void cleanUserConversationIdCache(List<String> userIds) {
        try {
            List<String> keys = new ArrayList<>();
            for (String userId : userIds) {
                keys.add(CONVERSATION_ID_LIST_CACHE + userId);
            }
            redisService.delKeys(keys);
        } catch (Exception e) {
            log.error("更新缓存中群成员信息异常,userIds{} ", JSONUtil.toJsonStr(userIds), e);
        }
    }


    @Override
    public void cleanUserConversationIdCache(String userId) {
        try {
            redisService.delKey(CONVERSATION_ID_LIST_CACHE + userId);
        } catch (Exception e) {
            log.error("更新缓存中群成员信息异常,userIds{} ", userId, e);
        }
    }

    @Override
    public void cleanContentCache(List<String> userIds, String conversationId) {
        try {
            List<String> contentCacheKeys = new ArrayList<>();
            for (String userId : userIds) {
                contentCacheKeys.add(getContentCacheKey(conversationId, userId));
            }
            redisService.delKeys(contentCacheKeys);
        } catch (Exception e) {
            log.error("更新缓存中群成员信息异常,userIds{} ", JSONUtil.toJsonStr(userIds), e);
        }
    }

    @Override
    public void updatePrivateLastMsgCache(String conversationId, String sender, String receiver) {
        try {
            String keySender = CONVERSATION_CONTENT_CACHE + conversationId + "_" + sender;
            String keyReceiver = CONVERSATION_CONTENT_CACHE + conversationId + "_" + receiver;

            redisService.deleteHashKey(keySender, "lastMsg");
            redisService.deleteHashKey(keySender, "lastMsgId");
            redisService.deleteHashKey(keyReceiver, "lastMsg");
            redisService.deleteHashKey(keyReceiver, "lastMsgId");

            String keyConversationIdList = CONVERSATION_ID_LIST_CACHE + receiver;
            redisService.delKey(keyConversationIdList);
        } catch (Exception e) {
            log.error("更新缓存中lastMsg异常,sender {}  receiver {}  ", sender, receiver, e);
        }
    }


    @Override
    public void updateConversationCache(String groupId, String userId) {
        try {
            Conversation conversation = getOne(new QueryWrapper<Conversation>()
                    .select("conversation_id", "chat_type").lambda()
                    .eq(Conversation::getUserId, userId)
                    .eq(Conversation::getChatgroupId, groupId));
            if (ChatType.CHAT_TYPE_MSG_HELPER.getNumber() == conversation.getChatType()) {
                log.debug("消息助手由apass处理，");
                return;
            }

            String key = getContentCacheKey(conversation.getConversationId(), userId);
            redisService.deleteHashKey(key, "lastMsg");
            redisService.deleteHashKey(key, "lastMsgId");

            //改会话置为已读的记录数，对应减去
            Integer unReadCount = getUnReadMsgCount(groupId, userId, conversation.getChatType());
            Integer totalUnReadMsgCount = getTotalUnReadMsgCount(userId);
            log.info("groupId: {} ：unReadCount: {}  totalUnReadMsgCount:{} ", groupId, unReadCount, totalUnReadMsgCount);

            redisService.setValue(PublicRedisKey.IM_UNREAD_MSG_COUNT + userId, Math.max((totalUnReadMsgCount - unReadCount), 0));
            redisService.deleteHashKey(key, "unReaderMsgCount");
            redisService.deleteHashKey(key, "lastAtAvatar");
        } catch (Exception e) {
            log.error("更新缓存中lastMsg异常,userId{} ", userId, e);
        }
    }


    public void updatePublicConversationMsgCache(String conversationId, String groupId) {
        try {
            List<ChatGroupMember> allMembers = groupMemberService.getAllMembers(groupId);
            if (CollectionUtils.isEmpty(allMembers)) {
                log.info("群groupId  {}  conversationID:{}  没有用户需要更新", groupId, conversationId);
                return;
            }
            List<String> updateUserIdList = allMembers.stream().map(ChatGroupMember::getUserId).collect(Collectors.toList());
            for (String userId : updateUserIdList) {
                String key = getContentCacheKey(conversationId, userId);
                redisService.deleteHashKey(key, "lastMsg");
                redisService.deleteHashKey(key, "lastMsgId");
                redisService.deleteHashKey(key, "unReaderMsgCount");
                redisService.deleteHashKey(key, "lastAtAvatar");
            }
        } catch (Exception e) {
            log.error("更新群  缓存中 lastMsg异常,groupId {} ", groupId, e);
        }
    }


    public void cleanUserAllConversationContent(List<String> conversationIdList, String userId) {
        List<String> keys = new ArrayList<>();
        for (String conversationId : conversationIdList) {
            String key = CONVERSATION_CONTENT_CACHE + conversationId + "_" + userId;
            keys.add(key);
        }
        redisService.delKeys(keys);
    }


    @Override
    public String getConversationIdByGroupId(String groupId, String userId) {
        LambdaQueryWrapper lambdaQueryWrapper = new QueryWrapper<Conversation>().select("conversation_id").lambda().eq(Conversation::getUserId, userId).eq(Conversation::getChatgroupId, groupId);
        Conversation conversation = getOne(lambdaQueryWrapper);
        return conversation != null ? conversation.getConversationId() : null;
    }

    @Override
    public String generateConversationList(List<ChatGroupMember> chatGroupMembers, ChatGroup chatGroup) {
        String conversationId = UUIDUtil.getUUID();
        Date now = new Date();
        Integer chatType = chatGroup.getChatType();
        String groupOwner = chatGroup.getGroupOwner();
        String groupId = chatGroup.getGroupId();

        List<Conversation> conversationList = new ArrayList<>();

        if (ChatType.CHAT_TYPE_PRIVATE.getNumber() == chatType) {
            int isSelfChat = isSelfChat(chatGroupMembers);
            ChatGroupMember ownerMember = chatGroupMembers.stream().filter(member -> member.getUserId().equals(groupOwner)).findAny().get();
            int ownnerIndex = chatGroupMembers.indexOf(ownerMember);
            ChatGroupMember oppositeMember = chatGroupMembers.get(1 - ownnerIndex);

            Conversation conversationOwner = new Conversation();
            conversationOwner.setConversationId(conversationId).setUserId(groupOwner).setChatgroupId(groupId).setChatType(chatType).setGroupOwner(groupOwner).setNoDisturb(YesOrNoEnum.NO.getCode()).setRoleType(MemberRoleType.OWNER.getCode()).setUnreadCount(0).setUpdatedTime(now).setSelfChat(isSelfChat).setCreatedTime(now);
            conversationOwner.setConversationName(oppositeMember.getUserName());
            conversationOwner.setAvatar(userAccountService.getAvatarUrl(oppositeMember.getUserId()));
            conversationOwner.setReceiver(oppositeMember.getUserId());

            Conversation conversationMember = new Conversation();
            conversationMember.setConversationId(conversationId).setUserId(oppositeMember.getUserId()).setChatgroupId(groupId).setChatType(chatType).setGroupOwner(groupOwner).setNoDisturb(YesOrNoEnum.NO.getCode()).setRoleType(MemberRoleType.MEMBER.getCode()).setUnreadCount(0).setUpdatedTime(now).setCreatedTime(now);
            conversationMember.setConversationName(ownerMember.getUserName());
            conversationMember.setSelfChat(isSelfChat);
            conversationMember.setAvatar(userAccountService.getAvatarUrl(groupOwner));
            conversationMember.setReceiver(groupOwner);

            if (YesOrNoEnum.YES.getCode() == isSelfChat) {
                //自己跟自己的会话,就只产生一条记录
                conversationList.add(conversationOwner);
            } else {
                conversationList.add(conversationMember);
                conversationList.add(conversationOwner);
            }
        } else {
            for (int i = 0; i < chatGroupMembers.size(); i++) {
                ChatGroupMember groupMember = chatGroupMembers.get(i);
                String userId = groupMember.getUserId();
                Conversation conversation = new Conversation();
                conversation.setConversationId(conversationId).setUserId(userId).setChatgroupId(groupId).setChatType(chatType).setGroupOwner(groupOwner).setNoDisturb(YesOrNoEnum.NO.getCode()).setRoleType(userId.equals(groupOwner) ? MemberRoleType.OWNER.getCode() : MemberRoleType.MEMBER.getCode()).setUnreadCount(0).setUpdatedTime(now).setCreatedTime(now);

                conversation.setConversationName(chatGroup.getGroupName());
                conversation.setAvatar(chatGroup.getAvatar());
                conversation.setReceiver(groupId);
                conversationList.add(conversation);
            }
        }
        saveBatch(conversationList);
        return conversationId;
    }

    @Override
    public String addConversationList(List<ChatGroupMember> chatGroupMembers, ChatGroup chatGroup, String updateUser) {
//        String conversationId = UUIDUtil.getUUID();
        Date now = new Date();
        Integer chatType = chatGroup.getChatType();
        String groupOwner = chatGroup.getGroupOwner();
        String groupId = chatGroup.getGroupId();

        String conversationId = getConversationIdByGroupId(groupId, updateUser);

        List<Conversation> conversationList = new ArrayList<>();
        for (int i = 0; i < chatGroupMembers.size(); i++) {
            ChatGroupMember groupMember = chatGroupMembers.get(i);
            String userId = groupMember.getUserId();
            Conversation conversation = new Conversation();
            conversation.setConversationId(conversationId).setUserId(userId).setChatgroupId(groupId).setChatType(chatType).setGroupOwner(groupOwner).setNoDisturb(YesOrNoEnum.NO.getCode()).setRoleType(userId.equals(groupOwner) ? MemberRoleType.OWNER.getCode() : MemberRoleType.MEMBER.getCode()).setUnreadCount(0).setUpdatedTime(now).setCreatedTime(now);
            conversation.setConversationName(chatGroup.getGroupName());
            conversation.setAvatar(chatGroup.getAvatar());
            conversation.setReceiver(groupId);
            conversationList.add(conversation);
        }
        saveBatch(conversationList);
        return conversationId;
    }

    @Override
    public void removeConversation(String groupId, List<String> userIdList) {
        QueryWrapper<Conversation> chatGroupMemberQueryWrapper = new QueryWrapper<>();
        chatGroupMemberQueryWrapper.lambda().eq(Conversation::getChatgroupId, groupId);
        chatGroupMemberQueryWrapper.lambda().in(Conversation::getUserId, userIdList);
        remove(chatGroupMemberQueryWrapper);
    }


    private int isSelfChat(List<ChatGroupMember> groupMemberList) {
        try {
            if (CollectionUtils.isNotEmpty(groupMemberList) && groupMemberList.size() == 2) {
                if (groupMemberList.get(0).getUserId().equals(groupMemberList.get(1).getUserId())) {
                    return YesOrNoEnum.YES.getCode();
                }
            }
        } catch (Exception e) {
            log.error("创建会话判断是否自己跟自己会话异常", e);
        }
        return YesOrNoEnum.NO.getCode();
    }

    @Override
    public void updateConversationNoDisturb(String userId, String conversationId, YesOrNoEnum yesOrNoEnum) {
        LambdaUpdateWrapper updateWrapper = new UpdateWrapper<Conversation>().set("no_disturb", yesOrNoEnum.getCode()).lambda().eq(Conversation::getConversationId, conversationId).eq(Conversation::getUserId, userId);
        update(updateWrapper);
        cleanContentCache(userId, conversationId);
    }

    @Override
    public void cleanContentCache(String userId, String conversationId) {
        try {
            String key = CONVERSATION_CONTENT_CACHE + conversationId + "_" + userId;
            redisService.delKey(key);
        } catch (Exception e) {
            log.error("更新缓存中群成员信息异常,userId {}  conversationId:{} ", userId, conversationId, e);
        }
    }

    @Override
    public Boolean fileHelperExist(String userId) {
        LambdaQueryWrapper queryWrapper = new QueryWrapper<Conversation>().lambda().eq(Conversation::getChatType, ChatType.FILE_HELPER.getNumber()).eq(Conversation::getUserId, userId);
        return count(queryWrapper) > 0;
    }

    private Conversation getFileHelper(String userId) {
        LambdaQueryWrapper queryWrapper = new QueryWrapper<Conversation>().lambda().eq(Conversation::getChatType, ChatType.FILE_HELPER.getNumber()).eq(Conversation::getUserId, userId);
        return getOne(queryWrapper);
    }


    @Override
    @Transactional
    public Conversation createFileHelper(String userId) {
        Conversation userConversation = null;
        try {
            if (null != (userConversation = getFileHelper(userId))) {
                log.error("该用户已创建文件共享助手");
                return userConversation;
            }

            String groupId = null;
            ChatGroup chatGroup = new ChatGroup.FileHelperBuilder(userId).build();
            chatGroup.setGroupName(ChatType.FILE_HELPER.getName());
            groupService.save(chatGroup);

            groupId = chatGroup.getGroupId();
            UserAccount userAccount = userAccountService.getByUserId(userId);
            String userName = userAccount.getUserName();

            ChatGroupMember userMember = new ChatGroupMember.FileHelperBuilder(userId, groupId, userName).build();
            ChatGroupMember imserverMember = new ChatGroupMember.FileHelperBuilder(IM_SERVER.USER_ID, groupId, ChatType.FILE_HELPER.getName(ChatType.FILE_HELPER)).build();
            List<ChatGroupMember> groupMemberList = Lists.newArrayList(userMember, imserverMember);
            groupMemberService.saveGroupMemberBatch(groupMemberList);

            userConversation = new Conversation.FileHelperBuilder(userId, groupId, IM_SERVER.USER_ID, ChatType.FILE_HELPER.getName(ChatType.FILE_HELPER)).build();
            Conversation imserverConversation = new Conversation.FileHelperBuilder(IM_SERVER.USER_ID, groupId, userId, userName).build();
            List<Conversation> conversationList = Lists.newArrayList(userConversation, imserverConversation);
            saveBatch(conversationList);

            //初始化提示消息
            ChatMsg chatMsg = new ChatMsg.TipBuilder(userConversation.getConversationId(), userId, groupId).build();
            msgService.save(chatMsg);
        } catch (Exception e) {
            log.error("userId :{} 创建文件助手失败", userId, e);
        }
        return userConversation;
    }


    /**
     * 数据完整性有可能被破坏，一个用户有可能创建了多条文件助手会话，待考证是什么原因，
     *
     * @param userId
     * @param userName
     * @return
     */
    @Override
    @Transactional
    public Conversation createFileHelper(String userId, String userName) {
/*        String userId = userAccount.getUserId();
        String userName = userAccount.getUserName();*/
        Conversation userConversation = null;
        if (null != (userConversation = getFileHelper(userId))) {
            log.info("该用户已创建文件助手");
            return userConversation;
        }

        String groupId = null;
        ChatGroup chatGroup = new ChatGroup.FileHelperBuilder(userId).build();
        groupService.save(chatGroup);

        groupId = chatGroup.getGroupId();
        ChatGroupMember userMember = new ChatGroupMember.FileHelperBuilder(userId, groupId, userName).build();
        ChatGroupMember imserverMember = new ChatGroupMember.FileHelperBuilder(IM_SERVER.USER_ID, groupId, ChatType.FILE_HELPER.getName(ChatType.FILE_HELPER)).build();
        List<ChatGroupMember> groupMemberList = Lists.newArrayList(userMember, imserverMember);
        groupMemberService.saveGroupMemberBatch(groupMemberList);

        userConversation = new Conversation.FileHelperBuilder(userId, groupId, IM_SERVER.USER_ID, ChatType.FILE_HELPER.getName(ChatType.FILE_HELPER)).build();
        Conversation imserverConversation = new Conversation.FileHelperBuilder(IM_SERVER.USER_ID, groupId, userId, userName).build();
        List<Conversation> conversationList = Lists.newArrayList(userConversation, imserverConversation);
        saveBatch(conversationList);

        //初始化提示消息
        ChatMsg chatMsg = new ChatMsg.TipBuilder(userConversation.getConversationId(), userId, groupId).build();
        msgService.save(chatMsg);
        return userConversation;
    }


    private Integer getMsgHelperUnReadCount(String seid) {
        String url = kuailuApiUrl + "j" + "?" + "appid=com.kuailu.base.apps.org&method=getUnReadNumber&seid=" + seid;
        String strResp = HttpUtil.doPostBody(url, "");
        return null;
    }


}
