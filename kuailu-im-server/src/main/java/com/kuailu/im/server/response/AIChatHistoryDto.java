package com.kuailu.im.server.response;

import com.kuailu.im.server.constant.IM_AI_SERVER;
import com.kuailu.im.server.enums.AIChatTypeEnum;
import com.kuailu.im.server.model.entity.BaseEntity;
import com.kuailu.im.server.util.UUIDUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 *
 */
@Data
@NoArgsConstructor
public class AIChatHistoryDto extends BaseEntity implements Serializable {

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


}
