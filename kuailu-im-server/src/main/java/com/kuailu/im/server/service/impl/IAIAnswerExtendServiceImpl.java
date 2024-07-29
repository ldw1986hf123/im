package com.kuailu.im.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kuailu.im.server.enums.AIAnswerStatusEnum;
import com.kuailu.im.server.mapper.AIAnswerExtendMapper;
import com.kuailu.im.server.model.entity.AIAnswerExtend;
import com.kuailu.im.server.service.IAIAnswerExtendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IAIAnswerExtendServiceImpl extends ServiceImpl<AIAnswerExtendMapper, AIAnswerExtend> implements IAIAnswerExtendService {


    @Override
    public AIAnswerExtend saveAnswer(String messageId, String content, String questionMsgId, String topicId, String receiver) {
        AIAnswerExtend answer = new AIAnswerExtend.AnswerBuilder(messageId, content, questionMsgId, topicId, receiver, AIAnswerStatusEnum.OK).build();
        save(answer);
        return answer;
    }
}
