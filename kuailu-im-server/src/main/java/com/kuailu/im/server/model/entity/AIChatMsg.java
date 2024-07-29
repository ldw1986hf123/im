package com.kuailu.im.server.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kuailu.im.core.packets.ChatType;
import com.kuailu.im.server.constant.IM_AI_SERVER;
import com.kuailu.im.server.constant.IM_SERVER;
import com.kuailu.im.server.enums.AIAnswerStatusEnum;
import com.kuailu.im.server.enums.AIChatTypeEnum;
import com.kuailu.im.server.enums.MessageTypeEnum;
import com.kuailu.im.server.enums.YesOrNoEnum;
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
@TableName("im_ai_chat_msg")
@NoArgsConstructor
public class AIChatMsg extends BaseEntity implements Serializable {

    /**
     * 消息ID
     */
    private String messageId;
    private String sender;
    private String senderName;
    private String receiver;
    private String content;
    private String questionMsgId;
    private String topicId;
    private Integer answerStatus;   //只有type是answer的时候，这个字段才有效
    private Integer type;
    private Integer operation;
    private String comment;

    @TableField(fill = FieldFill.INSERT)
    private Date createdTime;


    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedTime;

    public AIChatMsg(QuestionBuilder builder) {
        this.messageId = builder.messageId;
        this.sender = builder.sender;
        this.senderName = builder.senderName;
//        this.setCreatedTime(new Date());
//        this.setUpdatedTime(new Date());
        this.topicId=builder.topicId;
        this.content = builder.content;
        this.receiver = IM_AI_SERVER.USER_ID;
        this.type = AIChatTypeEnum.QUESTION.getCode();
        this.questionMsgId = "";
        this.setCreatedBy(sender);
        this.setUpdatedBy(sender);
    }

    public AIChatMsg(AnswerBuilder builder) {
        this.messageId =builder.messageId;
        this.sender = IM_AI_SERVER.USER_ID;
        this.senderName = IM_AI_SERVER.USER_ID;
        this.setCreatedTime(new Date());
        this.content = builder.content;
        this.type = AIChatTypeEnum.ANSWER.getCode();
        this.questionMsgId = builder.questionMsgId;
        this.topicId=builder.topicId;
        this.receiver=builder.receiver;
        this.setCreatedBy(sender);
        this.setUpdatedBy(sender);
    }

    public static class QuestionBuilder {
        private String messageId;
        private String sender;
        private String senderName;
        private String content;
        private String topicId;

        public QuestionBuilder(String messageId,
                               String sender, String senderName, String content, String topicId) {
            this.messageId = messageId;
            this.sender = sender;
            this.senderName = senderName;
            this.content = content;
            this.topicId = topicId;
        }
        public AIChatMsg build() {
            return new AIChatMsg(this);
        }
    }

    public static class AnswerBuilder {
        private String messageId;
        private String content;
        private String questionMsgId;
        private String topicId;
        private String receiver;

        public AnswerBuilder(String messageId, String content, String questionMsgId, String topicId,String receiver) {
            this.messageId=messageId;
            this.content = content;
            this.questionMsgId = questionMsgId;
            this.topicId = topicId;
            this.receiver = receiver;

        }
        public AIChatMsg build() {
            return new AIChatMsg(this);
        }
    }


    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getQuestionMsgId() {
        return questionMsgId;
    }

    public void setQuestionMsgId(String questionMsgId) {
        this.questionMsgId = questionMsgId;
    }

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public Integer getAnswerStatus() {
        return answerStatus;
    }

    public void setAnswerStatus(Integer answerStatus) {
        this.answerStatus = answerStatus;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }



    @Override
    public Date getCreatedTime() {
        return createdTime;
    }

    @Override
    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }


    @Override
    public Date getUpdatedTime() {
        return updatedTime;
    }

    @Override
    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }


    public Integer getOperation() {
        return operation;
    }

    public void setOperation(Integer operation) {
        this.operation = operation;
    }
}
