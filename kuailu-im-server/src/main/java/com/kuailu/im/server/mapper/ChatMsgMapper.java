package com.kuailu.im.server.mapper;

import com.kuailu.im.server.model.entity.ChatMsg;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 */
public interface ChatMsgMapper extends BaseMapper<ChatMsg> {
    Integer getPrivateUserGroupUnReadMsgCount(@Param("userId") String userId,
                                              @Param("groupId") String groupId,
                                              @Param("messageId") String messageId);

    Integer getPublicUserGroupUnReadMsgCount(@Param("userId") String userId,
                                             @Param("groupId") String groupId,
                                             @Param("messageId") String messageId);


    List<ChatMsg> getConversationList(@Param("sender") String sender);

    List<Map<String, Object>> getPrivateChatRecords(@Param("userId") String userId, @Param("searchKey") String searchKey);

    List<Map<String, Object>> getPublicChatRecords(@Param("userId") String userId, @Param("searchKey") String searchKey);

    List<Map<String, Object>> getPublicChatRecordDetail(@Param("userId") String userId, @Param("searchKey") String searchKey, @Param("groupId") String groupId);

    List<Map<String, Object>> getPrivateChatRecordDetail(@Param("userId") String userId, @Param("searchKey") String searchKey, @Param("groupId") String groupId);


}
