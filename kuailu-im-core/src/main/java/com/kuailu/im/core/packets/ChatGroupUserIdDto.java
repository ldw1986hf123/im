package com.kuailu.im.core.packets;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @description:
 * @author: 林坚丁
 * @time: 2023/2/10 14:30
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatGroupUserIdDto implements Serializable {
    private static final long serialVersionUID = -3817755433171220945L;
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
     * 头像
     */
    private  String  avatar;

    /**
     * 群名称
     */
    private String groupName;

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
    private LocalDateTime startMuteTime;

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

    private LocalDateTime createdTime;

    private String updatedBy;

    private LocalDateTime updatedTime;

    private String otherUserIds;


}
