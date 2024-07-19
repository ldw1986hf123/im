package com.kuailu.im.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.kuailu.im.server.model.entity.AIChatMsg;
import com.kuailu.im.server.service.IAIChatMsgService;
import com.kuailu.im.server.starter.BaseJunitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AiChatMsgServiceImplTest extends BaseJunitTest {
    @Autowired
    IAIChatMsgService iaiChatMsgService;

    @Test
    void saveQuestion() {
        AIChatMsg aiChatMsg = new AIChatMsg.QuestionBuilder("messid123ccc", "ldw", "ldw", "saveOrUpdate test", "topicId11").build();

        LambdaUpdateWrapper<AIChatMsg> updateWrapper = new UpdateWrapper<AIChatMsg>().lambda()
                .eq(AIChatMsg::getMessageId, "messid123ccc");

        iaiChatMsgService.saveOrUpdate(aiChatMsg, updateWrapper);
    }


}