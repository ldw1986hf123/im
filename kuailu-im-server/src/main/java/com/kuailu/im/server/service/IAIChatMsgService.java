package com.kuailu.im.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.kuailu.im.core.param.ApassChatReqParam;
import com.kuailu.im.server.model.ResponseModel;
import com.kuailu.im.server.model.entity.AIChatMsg;
import com.kuailu.im.server.model.entity.ChatMsg;
import com.kuailu.im.server.processor.param.AITrampleParam;
import com.kuailu.im.server.processor.param.RevokeMessageParam;
import com.kuailu.im.server.req.AIChatReqParam;
import com.kuailu.im.server.req.MessageBody;
import com.kuailu.im.server.response.MessageHistoryResponse;

import java.util.List;
import java.util.Map;

/**
 * <p>
 */
public interface IAIChatMsgService extends IService<AIChatMsg> {

    void saveQuestion(AIChatReqParam aiChatReqParam);

    Boolean hasAnswer(String questionMsgId);

    AIChatMsg saveAnswer(String messageId,  String content, String questionMsgId, String topicId, String receiver);

    ResponseModel history(String userId, String messageId,String topicId, int count);

    ResponseModel getUserStatus(String userId);

    ResponseModel closeTopic(String userId, String topicId);

    ResponseModel continueTopic(String userId);

    ResponseModel praise(String messageId, Integer operation, String comment);

//    ResponseModel trample(AITrampleParam messageId);
    ResponseModel feedbackList();

    ResponseModel commandCenter();
}
