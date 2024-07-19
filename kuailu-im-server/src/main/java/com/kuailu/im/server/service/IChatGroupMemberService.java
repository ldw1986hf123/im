package com.kuailu.im.server.service;

import com.kuailu.im.core.packets.ChatGroupUserIdDto;
import com.kuailu.im.core.packets.PrivateGroupDto;
import com.kuailu.im.server.dto.GroupCacheDto;
import com.kuailu.im.server.enums.MemberRoleType;
import com.kuailu.im.server.model.entity.ChatGroupMember;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kuailu.im.server.vo.ChatGroupVo;

import java.util.List;
import java.util.Map;

/**
 * <p>
 */
public interface IChatGroupMemberService extends IService<ChatGroupMember> {
    PrivateGroupDto getPrivateGroup(String userId, String memberId);

    ChatGroupMember getNewGroupName(String groupId);

    /**
     * 找单聊中对方的人的信息
     * @param groupId
     * @param currentUserId
     * @return
     */
//    ChatGroupMember getPrivateChatterMember(String groupId, String currentUserId) ;


    /**
     * 获取群组所有成员
     *
     * @param groupId
     * @return
     */
    List<ChatGroupMember> getAllMembers(String groupId);


    /**
     * 获取用户加入的所有群组id
     * @param userId
     * @return
     */
//    List<String>  getAllJoinGroupId(String userId);


    List<String> getAllJoinGroupId(String userId);

    /**
     * 判断用户是否再群里
     *
     * @param groupId
     * @param userId
     * @return
     */
    Boolean isInGroup(String groupId, String userId);

//    Map<String, List<ChatGroupMember>> getMemberByGroupId(List<String> groupIdList);


    /**
     * 根据groupIdList 获取私聊对话人
     *
     * @param groupIdList
     * @return
     */
    Map<String, ChatGroupMember> getPrivateChatterMember(List<String> groupIdList, String currentUserId);


    /**
     * 根据groupIdList 获取自己跟自己的会话
     *
     * @param groupIdList
     * @return
     */
    void getSelfChat(List<String> groupIdList, List<ChatGroupMember> memberList);

    List<GroupCacheDto> getUserGroups(String userId);

    /**
     * 只再登录的时候调用
     *
     * @param userId
     */
    List<GroupCacheDto> cacheLoginUserGroups(String userId);


    void cleanGroupMemberCache(String groupId);

    /**
     * 根据groupID 获取某个群在线用户的id;
     *
     * @param groupId
     * @return
     */
    List<String> getAllOnlineMembers(String groupId);


    void cleanUserUserGroupCache(String userId);

    Boolean isSelfChat(String groupId);

    /**
     * 拿到自己再私聊中的会话
     * @param userId
     * @param groupId
     * @return
     */
    String getRoleTypeInPrivateChat(String userId, String groupId);

    void saveGroupMemberBatch(List<ChatGroupMember> inviteMembers);

    ChatGroupMember getPrivateChatter(String userId, String groupId);
}
