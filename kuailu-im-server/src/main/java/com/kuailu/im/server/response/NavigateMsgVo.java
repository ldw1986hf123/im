package com.kuailu.im.server.response;

import lombok.Data;

import java.util.Date;
@Data
public class NavigateMsgVo   {
    private Long id;
    /**
     * 消息ID
     */
    private String messageId;

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
    private String msg;

    private String groupId;


    /**
     * 0:正常，1:删除
     */
    private Integer status;

    /**
     * 消息发送时间
     */

    private Date createdTime;


    /**
     * 0:未读，1:已读
     */
    private Integer isRead;


}
