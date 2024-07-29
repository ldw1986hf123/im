package com.kuailu.im.server.response;

import com.kuailu.im.server.constant.IM_AI_SERVER;
import com.kuailu.im.server.enums.AIChatTypeEnum;
import com.kuailu.im.server.enums.AiMessageTypeEnum;
import com.kuailu.im.server.model.entity.AIChatMsg;
import com.kuailu.im.server.req.ChatReqParam;
import com.kuailu.im.server.req.MessageBody;
import com.kuailu.im.server.util.UUIDUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;

/**
 *
 */
@Data
public class AIChatResponse implements Serializable {
    /**
     * 用户ID
     */

    @Deprecated
    Integer aiMessageType;//todo 已废弃
    List<Map<String, String>> content;
    String questionMsgId;
    String messageId;
    String topicId;
    private Long createdTime;


    List<String> recommendCommand = new ArrayList<>();//推荐指令

    public AIChatResponse(AIChatResponse.TextResponseBuilder builder) {
        this.aiMessageType = AiMessageTypeEnum.TEXT.getCode();
        this.messageId = builder.messageId;
        this.questionMsgId = builder.questionMsgId;
        this.topicId = builder.topicId;
        this.createdTime = new Date().getTime();
    }

    /* public AIChatResponse(AIChatResponse.SqlResponseBuilder builder) {
         this.aiMessageType = AiMessageTypeEnum.SQL.getCode();
         this.messageId = UUIDUtil.getUUID();
         this.questionMsgId = builder.questionMsgId;
         this.topicId = builder.topicId;
         this.createdTime = new Date().getTime();
     }
 */
    public static class TextResponseBuilder {
        private String questionMsgId;
        private String topicId;
        private String messageId;

        public TextResponseBuilder(String questionMsgId, String topicId, String messageId) {
            this.questionMsgId = questionMsgId;
            this.topicId = topicId;
            this.messageId = messageId;
        }

        public AIChatResponse build() {
            return new AIChatResponse(this);
        }
    }
   /* public static class SqlResponseBuilder {
        private String questionMsgId;
        private String topicId;

        public SqlResponseBuilder(String questionMsgId, String topicId) {
            this.questionMsgId = questionMsgId;
            this.topicId = topicId;
        }

        public AIChatResponse build() {
            return new AIChatResponse(this);
        }
    }*/


}
