package com.kuailu.im.server.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import com.kuailu.im.core.packets.ChatType;
import com.kuailu.im.server.constant.IM_SERVER;
import com.kuailu.im.server.enums.MemberRoleType;
import com.kuailu.im.server.enums.YesOrNoEnum;
import com.kuailu.im.server.util.UUIDUtil;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.tio.core.Tio;

/**
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("im_conversation")
@NoArgsConstructor
public class Conversation implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String userId;
    /**
     * 应用Key
     */
    private String appKey;

    /**
     * 会话ID, 群编号
     */
    private String conversationId;

    /**
     * 群组ID
     */
    private String chatgroupId;

    private String lastMsgId;

    /**
     * 0:非群聊, 1:是群聊
     */
    private Integer chatType;


    private String roleType;
    private Integer selfChat = YesOrNoEnum.NO.getCode();
    private Integer unreadCount;
    private Integer noDisturb;
    private String conversationName;
    private String receiver;
    /**
     * 对方的头像
     */
    private String avatar;
    /**
     * 群主
     */
    private String groupOwner;

    private String createdBy;

    private Date createdTime;

    private String updatedBy;

    private Date updatedTime;

    public Conversation(FileHelperBuilder builder) {
        this.conversationId = UUIDUtil.getUUID();
        this.conversationName = builder.conversationName;
        this.chatType = ChatType.FILE_HELPER.getNumber();
        this.receiver = builder.receiver;
        this.chatgroupId = builder.chatgroupId;
        this.userId = builder.userId;
        this.createdTime = new Date();
        this.groupOwner = builder.userId;
        this.updatedTime = new Date();
        this.createdBy = "IM-SERVER";
        this.roleType = builder.userId.equals(IM_SERVER.USER_ID) ? MemberRoleType.MEMBER.getCode() : MemberRoleType.OWNER.getCode();
    }


    public static class FileHelperBuilder {
        private String chatgroupId;//必须
        private String userId;//可选
        private String receiver;//可选
        private String conversationName;

        public FileHelperBuilder(String userId, String chatgroupId, String receiver, String conversationName) {
            this.chatgroupId = chatgroupId;
            this.userId = userId;
            this.receiver = receiver;
            this.conversationName = conversationName;

        }

        public Conversation build() {
            return new Conversation(this);
        }
    }


}
