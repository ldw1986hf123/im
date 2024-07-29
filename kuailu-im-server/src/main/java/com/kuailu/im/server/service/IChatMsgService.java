package com.kuailu.im.server.service;

import com.kuailu.im.core.param.ApassChatReqParam;
import com.kuailu.im.server.model.ResponseModel;
import com.kuailu.im.server.model.entity.ChatMsg;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kuailu.im.server.processor.param.RevokeMessageParam;
import com.kuailu.im.server.req.ChatReqParam;
import com.kuailu.im.server.req.MessageBody;
import com.kuailu.im.server.response.MessageHistoryResponse;

import java.util.List;
import java.util.Map;

/**
 * <p>
 */
public interface IChatMsgService extends IService<ChatMsg> {

    /**
     * 获取用户的未读消息数
     *
     * @param currentUserId
     * @return
     */
    int getGroupUnreadCount(String groupId, String currentUserId);

    /**
     * 把某条消息之前的消息都已读
     *
     * @param currentUserId
     * @param messageId
     * @return
     */
    void confirmReadMsg(String currentUserId, String groupId, String messageId);

    ResponseModel revoke(RevokeMessageParam revokeMessageParam);

    MessageHistoryResponse buildChatList(List<ChatMsg> chatMsgList, String receiver, String groupId);


    MessageBody buildRedirectMergeMessage(ChatMsg chatMsg);

    MessageBody buildMergeRedirectMessage(String messageId, String groupId);

    MessageBody buildMessageBody(List<ChatMsg> chatMsgList);

    ChatMsg getByMessageId(String groupId, String messageId);

    List<ChatMsg> getMessageHistory(Integer count, String groupId, Long endTime);

    int getPrivateUnreadCountByGroupId(String groupId, String currentUserId);

    ChatMsg getLastMsg(String groupId);

    void cleanMsgContent(String groupId, String messageId);

    void cleanMsgContent(String groupId, List<String> messageIdList);


    List<ChatMsg> navigate(String messageId, String groupId, Integer count);

    List<ChatMsg> getMessageContext(Integer count, String groupId, String messageId, String direction);

    MessageHistoryResponse buildChatList(List<ChatMsg> chatMsgList, String groupId);

    List<ChatMsg> getMessageContext(Integer count, String groupId);

    List<Map<String, Object>> getPrivateChatRecords(String userId, String searchKey);

    List<Map<String, Object>> getPublicChatRecords(String userId, String searchKey);

    List<Map<String, Object>> getPublicChatRecordDetail(String userId, String searchKey, String groupId);

    List<Map<String, Object>> getPrivateChatRecordDetail(String userId, String searchKey, String groupId);

    List<Map<String, Object>> getChatRecordResps (String userId, String searchKey);

    Boolean deleteByMessageId(String messageId, String groupId);

    void cleanApassMessageCache( );

    String send(String messageId,String sender, String receiver, String groupId, String conversationId, ApassChatReqParam.MessageBody messageBody, Integer msgType);

    void updateUnReadMsgCount(String groupId,String currentUserId);
}
