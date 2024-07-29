package com.kuailu.im.server.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.kuailu.im.server.dto.ConversationCacheDto;
import com.kuailu.im.server.enums.YesOrNoEnum;
import com.kuailu.im.server.model.entity.ChatGroup;
import com.kuailu.im.server.model.entity.ChatGroupMember;
import com.kuailu.im.server.model.entity.Conversation;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kuailu.im.server.model.entity.UserAccount;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 */
public interface IConversationService extends IService<Conversation> {
    List<ConversationCacheDto> getCurrentUserConversations(List<String> groupIds, String userId,String seid);

    @Deprecated
    List<String> getConversationIdByGroupId(List<String> groupIds, String userId);

    /**
     * 会话要带上用户属性，每个用户，有自己对于的会话
     *
     * @param groupIds
     * @param userId
     * @return
     */
    List<String> getConversationIdUserId(List<String> groupIds, String userId);


    Integer getTotalUnReadMsgCount(String currentUserId);

    /**
     * 推送建群成功通知给前端
     *
     * @param groupId
     */
    void sendConversation(String conversationName, String groupId, String conversationId, Long createdTime, Integer chatType, String owner, String avatar);

    void cleanUserConversationIdCache(List<String> userIds);

    void cleanUserConversationIdCache(String userId);

    void cleanContentCache(List<String> userIds, String conversationId);

//    void updateUnReadCountCache(String conversationId, String userId);

    void updatePublicConversationMsgCache(String conversationId, String groupId);

    void updateConversationCache(String groupId, String userId);

    void updatePrivateLastMsgCache(String conversationId, String sender, String receiver);

    void cleanUserAllConversationContent(List<String> conversationIdList, String userId);

    String getConversationIdByGroupId(String groupId, String userId);

    String generateConversationList(List<ChatGroupMember> chatGroupMembers, ChatGroup chatGroup);

    void updateConversationNoDisturb(String userId, String conversationId, YesOrNoEnum yesOrNoEnum);

    void cleanContentCache(String userId, String conversationId);

    Conversation getFileHelperByUserId(String userId);

    String getReceiver(String userId, String conversationId);

    String addConversationList(List<ChatGroupMember> chatGroupMembers, ChatGroup chatGroup, String updateUser);

    void removeConversation(String groupId, List<String> userIdList);

//    String getLastAtAvatarByConversationId(String conversationId, String groupId, String userId);

    void updateLastMessage(String groupId, String userId, String lastMsgId);


    Boolean fileHelperExist(String userId);


    /**
     * 创建一个文件助手会话
     * @param userId
     */
    Conversation createFileHelper(String userId);

    @Transactional
    Conversation createFileHelper(String userId, String userName);
}
