package com.kuailu.im.server.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Date;

import com.kuailu.im.core.packets.ChatType;
import com.kuailu.im.server.util.UUIDUtil;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("im_chat_group")
@NoArgsConstructor
public class ChatGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 应用Key
     */
    private String appKey;

    /**
     * 业务Key
     */
    private String bizType;

    /**
     * 群编号
     */
    private String groupId;

    /**
     * 群名称
     */
    private String groupName;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 0:非群聊, 1:是群聊
     */
    private Integer chatType;

    /**
     * 群主
     */
    private String groupOwner;

    /**
     * 群成员上限, 默认30
     */
    private Integer maxMembers;

    /**
     * 0:禁言, 1:正常
     */
    private String status;

    /**
     * 禁言开始时间
     */
//    private LocalDateTime startMuteTime;

    /**
     * 禁言多长时间，单位秒，-1:表示永久
     */
    private Long muteDuration;

    /**
     * 群描述
     */
    private String description;

    /**
     * 群扩展信息
     */
    private String ext;

    private String createdBy;

    private Date createdTime;

    private String updatedBy;
    private Date updatedTime;


    public ChatGroup(FileHelperBuilder builder) {
        this.groupId = UUIDUtil.getUUID();
        this.groupOwner = builder.groupOwner;
        this.chatType = ChatType.FILE_HELPER.getNumber();
        this.groupName = "";
        this.createdTime = new Date();
        this.updatedTime = new Date();
        this.createdBy = builder.groupOwner;
    }


    public static class FileHelperBuilder {
        private String groupOwner;

        public FileHelperBuilder(String userId) {
            this.groupOwner = userId;
        }

        public ChatGroup build() {
            return new ChatGroup(this);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof ChatGroup) {
            ChatGroup chatGroup = (ChatGroup) obj;
            if (chatGroup.getGroupId().equals(this.groupId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getGroupId().hashCode();
    }

}
