package com.kuailu.im.server.response;

import com.kuailu.im.server.req.ChatReqParam;
import com.kuailu.im.server.req.MessageBody;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
@Data
public class MergeEntityDetailResponse implements Serializable {
    //合并的每一条消息的id
    private String id;
    /**
     * 消息ID
     */

    /**
     * 会话ID, 群编号
     */
    private String conversationId;

    /**
     * 0:非群聊, 1:是群聊
     */
    private Integer chatType;

    /**
     * 消息发送者
     */
    private String sender;
    /**
     * 消息发送者姓名
     */
    private String senderName;

    /**
     * 消息接收者, 会话ID
     */
    private String receiver;

    /**
     * 消息类型，0:文本，1:图片，2:语音，3:视频，4:音乐,5:图文 file:文件,,loc:地址位置
     */
    private Integer msgType;

    private String operaType;

    /**
     * 消息内容
     */
    private MessageBody messageBody;

    private String createdBy;

    private Long createdTime;

}
