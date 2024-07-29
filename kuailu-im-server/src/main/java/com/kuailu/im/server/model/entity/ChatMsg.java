package com.kuailu.im.server.model.entity;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kuailu.im.core.packets.ChatType;
import com.kuailu.im.core.param.ApassChatReqParam;
import com.kuailu.im.server.constant.IM_SERVER;
import com.kuailu.im.server.enums.MessageTypeEnum;
import com.kuailu.im.server.enums.YesOrNoEnum;
import com.kuailu.im.server.util.UUIDUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("im_chat_msg")
@NoArgsConstructor
@Data
public class ChatMsg implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 消息类型，0:文本，1:图片，2:语音，3:视频，4:音乐,5:图文 file:文件,,loc:地址位置,6:OA待办
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
    private Date sendTime;

    /**
     * pending: 0, // 发送未开始 delivering: 1, // 正在发送 succeed: 2, // 发送成功 failed: 3, // 发送失败
     */
    private Integer msgSendStatus;


    private String createdBy;

    private Date createdTime;

    private String updatedBy;

    private Date updatedTime;

    private String showSide;

    /**
     * 0:未读，1:已读
     */
    private Integer isRead;

    private String msgContent;


    public ChatMsg(String messageId, String sender, String senderName, Integer chatType, String conversationId, String msg,
                   String operaType, String receiver, Integer msgType, String groupId, Long createdTime, String msgContent) {
        this.messageId = messageId;
        this.chatType = chatType;
        this.sender = sender;
        this.createdTime = new Date(createdTime);
        this.sendTime = new Date();
        this.senderName = senderName;
        this.msg = msg;
        this.conversationId = conversationId;
        this.operaType = operaType;
        this.receiver = receiver;
        this.msgType = msgType;
        this.groupId = groupId;
        this.isRead = (sender.equals(receiver) ? YesOrNoEnum.YES.getCode() : YesOrNoEnum.NO.getCode());
        this.msgContent = msgContent;
    }

    public ChatMsg(FileHelperBuilder builder) {
        this.messageId = builder.messageId;
        this.chatType = builder.chatType;
        this.sender = builder.sender;
        this.createdTime = new Date();
        this.sendTime = new Date();
        this.senderName = builder.senderName;
        this.msg = builder.msg;
        this.conversationId = builder.conversationId;
        this.receiver = builder.receiver;
        this.msgType = builder.msgType;
        this.groupId = builder.groupId;
        this.isRead = YesOrNoEnum.YES.getCode();
        this.msgContent = builder.msgContent;
    }

    public ChatMsg(TipBuilder builder) {
        this.messageId = builder.messageId;
        this.chatType = ChatType.FILE_HELPER.getNumber();
        this.sender = IM_SERVER.USER_ID;
        this.createdTime = new Date();
        this.sendTime = new Date();
        this.senderName = "文件共享助手";
        this.msgType = MessageTypeEnum.TIPS.getCode();
        String tipMsg=    " {\n" +
                "            \"content\": \"您可以使用[文件共享助手]与电脑互传文件\"\n" +
                "        }";
        this.msg = tipMsg;
        this.conversationId = builder.conversationId;
        this.receiver = builder.receiver;
        this.groupId = builder.groupId;
        this.isRead = YesOrNoEnum.NO.getCode();
    }

    public static class FileHelperBuilder {
        private String messageId;
        private String conversationId;
        private Integer chatType;
        private String sender;
        private String senderName;
        private String receiver;
        private Integer msgType;
        private String msg;
        private String groupId;

        private String msgContent;

        public FileHelperBuilder(String sender, String senderName, Integer chatType, String conversationId, ApassChatReqParam.MessageBody messageBody,
                                 String receiver, Integer msgType, String groupId ) {
            this.conversationId = conversationId;
            this.messageId = UUIDUtil.getUUID();
            this.chatType = chatType;
            this.sender = sender;
            this.senderName = senderName;
            this.receiver = receiver;
            this.msgType = msgType;
            this.msg = JSONUtil.toJsonPrettyStr(messageBody);
            this.groupId = groupId;
        }

        public ChatMsg build() {
            return new ChatMsg(this);
        }

    }

    public static class TipBuilder {
        private String messageId;
        private String conversationId;
        private String receiver;
        private String groupId;

        public TipBuilder(String conversationId,
                          String receiver, String groupId) {
            this.conversationId = conversationId;
            this.messageId = UUIDUtil.getUUID();
            this.receiver = receiver;
            this.groupId = groupId;
        }

        public ChatMsg build() {
            return new ChatMsg(this);
        }
    }

}
