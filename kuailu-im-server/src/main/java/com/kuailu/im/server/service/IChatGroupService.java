package com.kuailu.im.server.service;

import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.exception.ImException;
import com.kuailu.im.server.dto.GroupCacheDto;
import com.kuailu.im.server.model.entity.ChatGroup;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kuailu.im.server.mq.PushMessage;
import com.kuailu.im.server.req.ChatReqParam;
import com.kuailu.im.server.vo.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 */
public interface IChatGroupService extends IService<ChatGroup> {

    ChatGroupDetailVo saveOrUpdateChatGroup(ChatGroupVo chatGroupVo);

//    ChatGroupDetailVo getDetailsByGroupId(String seid,String groupId);

    ChatGroupDetailVo memberInviteChatGroup(ChatGroupMemberOperationVO operationVO);

    void memberRemoveChatGroup(ChatGroupMemberOperationVO operationVO);
    ChatGroupDetailVo getDetailsByGroupId(String groupId,String userId);
    ChatGroupDetailVo getDetailsByGroupId(String groupId);
    void initBindUserGroup(String userId, List<String> groupIdList);

    void memberRemoveChatGroup(String groupId, List<String> removedUserIdList);

    ChatGroup getByGroupId(String groupId);

//    ChatGroup getMsgNotify(PushMessage pushMessage, String lastMsgId) throws InterruptedException;

    ChatGroup createMsgHelperGroup(String receiver);

    void createMsgHelperGroupMember(ChatGroup chatGroup);

    void createMsgHelperConversation(String receiver, String groupId);

    List<ChatGroupResp> getChatGroupResps (String userId, String searchKey);

}
