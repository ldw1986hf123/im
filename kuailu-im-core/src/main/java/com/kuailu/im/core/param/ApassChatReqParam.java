/**
 *
 */
package com.kuailu.im.core.param;

import lombok.Data;

import java.util.List;
import java.util.Map;


@Data
public class ApassChatReqParam {

    private static final long serialVersionUID = 5731474214655476286L;
    /**
     * todo 发送用户id  不需要前端传;
     */
    private String sender;
    private String seid;

    //todo 不需要前端传
    private String senderName;

    private String mergedUserName;

    /**
     * 目标用户id;
     */
    private String receiver;
    /**
     * msgType：0:文本、1:文件、2:语音、3:视频、 4：合并转发消息、5、图片
     */
    private Integer msgType;
    /**
     * 聊天类型;(如公聊、私聊)
     */
    private Integer chatType;
    /**
     * 消息内容;
     */
    private MessageBody messageBody;
    /**
     * 消息发到哪个群组;
     */
    private String groupId;

    private Long timestamp;

    private Integer status;

    /**
     * 合并消息的时候，才用到这个属性
     */
    private List<String> mergedMessageIdList;

//	private String mergedEntityId;

    private String mergedMessageId;

    //合并转发，还是转发合并
    private String operaType;

    //消息是显示左边还说右边
    private String showSide;

    private String conversationId;

    @Data
    public class MessageBody {
        private String content;
        private List<Map<String, Object>> mergeMessageList;
        private String mergedTitle;
        private Integer mergeLevel;
        private String mergeEntityId;
        private String fullName;
        private String pdgThumbViewer;
        private Long fileSize;
        private String suffix;
        private Long duration;
        private String mainFile;
        private String attachFile;
        private String cover;
        private Integer orientation;
        private Integer chatType;
        private Integer original;
        private List<Map<String, Object>> atUsersInfo;  //@所有有人 AtAllUserTag
        private List<String> atUserList;   //@所有有人 AtAllUserTag

        private String atUserIdContent;   // userId 替换了用户名之后的消息内容
        private boolean isAtSelf;
    }

}
