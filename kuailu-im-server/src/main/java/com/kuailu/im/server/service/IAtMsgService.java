package com.kuailu.im.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kuailu.im.server.model.entity.AtMsg;
import com.kuailu.im.server.model.entity.ChatMsg;
import com.kuailu.im.server.req.MessageBody;
import com.kuailu.im.server.response.MessageHistoryResponse;

import java.util.List;

/**
 * <p>
 */
public interface IAtMsgService extends IService<AtMsg> {

    List<String> getAllReadAtUserByMessage(MessageBody messageBody, String messageId, String groupId);

    void saveAtMsg(MessageBody messageBody, String messageId, String sender, String groupId, String conversationId);




    void readAll(String userId, String groupId);

    MessageHistoryResponse.HistoryChat getLastUnReadMsg(String groupId, String userId);

    List<String> extractAtUserIdInMag(ChatMsg chatMsg, String groupId);
}
