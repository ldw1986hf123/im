package com.kuailu.im.server.response;

import com.kuailu.im.server.req.MessageBody;
import lombok.Data;

@Data
public class MessageContextVo {
    private String messageId;
    private String sender;
    private String senderName;
    private String receiver;
    private Integer msgType;
    private Integer chatType;
    private MessageBody messageBody;
    protected Long createdTime;
    private String groupId;
    private Integer status;
    private Integer isRead;
}
