package com.kuailu.im.server.response;

import com.kuailu.im.server.enums.MessageStatusEnum;
import lombok.Data;

import java.io.Serializable;


/**
 * 消息撤回推送类
 */
@Data
public class RevokeMessageResponse implements Serializable {
    private String messageId;
    private Integer status;

    public RevokeMessageResponse(String messageId,MessageStatusEnum messageStatusEnum) {
        this.messageId = messageId;
        this.status=messageStatusEnum.getCode();
    }

}
