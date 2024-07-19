package com.kuailu.im.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kuailu.im.server.model.entity.AIAnswerExtend;
import com.kuailu.im.server.model.entity.AIChatMsg;
import com.kuailu.im.server.req.AIChatReqParam;

/**
 * <p>
 */
public interface IAIAnswerExtendService extends IService<AIAnswerExtend> {

    AIAnswerExtend saveAnswer(String messageId,String content, String questionMsgId, String topicId,String receiver);
}
