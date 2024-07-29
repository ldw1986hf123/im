package com.kuailu.im.server.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.kuailu.im.server.constant.IM_AI_SERVER;
import com.kuailu.im.server.enums.AIAnswerStatusEnum;
import com.kuailu.im.server.util.UUIDUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 *
 */
@Accessors(chain = true)
@TableName("im_ai_answer_extend")
@Data
@NoArgsConstructor
public class AIAnswerExtend extends BaseEntity implements Serializable {
    /**
     * 消息ID
     */
    private String messageId;
    private String questionMsgId;
    private String receiver;
    private String content;
    private String topicId;
    private Integer answerStatus;
    private Integer operation;
    private String comment;

    public AIAnswerExtend(AnswerBuilder builder) {
        this.messageId = builder.messageId;
        this.setCreatedTime(new Date());
        this.content = builder.content;
        this.receiver = builder.receiver;
        this.topicId = builder.topicId;
        this.questionMsgId = builder.questionMsgId;
        this.answerStatus = builder.answerStatus;
        this.setCreatedBy(IM_AI_SERVER.USER_ID);
        this.setUpdatedBy(IM_AI_SERVER.USER_ID);
    }


    public static class AnswerBuilder {
        /**
         * 消息ID
         */
        private String messageId;
        private String content;
        private String questionMsgId;
        private String topicId;
        private Integer answerStatus;
        private String receiver;

        public AnswerBuilder(String messageId, String content, String questionMsgId, String topicId, String receiver, AIAnswerStatusEnum aiAnswerStatusEnum) {
            this.messageId = messageId;
            this.content = content;
            this.questionMsgId = questionMsgId;
            this.topicId = topicId;
            this.answerStatus = aiAnswerStatusEnum.getCode();
            this.receiver = receiver;
        }

        public AIAnswerExtend build() {
            return new AIAnswerExtend(this);
        }
    }
}
