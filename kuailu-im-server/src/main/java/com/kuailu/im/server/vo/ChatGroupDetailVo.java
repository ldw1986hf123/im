package com.kuailu.im.server.vo;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 */

@Accessors(chain = true)
@EqualsAndHashCode(callSuper=false)
@Data
@Builder
public class ChatGroupDetailVo  implements Serializable {
    private static final long serialVersionUID = 4379982384714504420L;
    private List<GroupDetailMemberVo> members;


    private String id;

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
     * 群名称
     */
    private String conversationName;
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


    private  String conversationId;
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
    private Date startMuteTime;

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

    private Long createdTime;

    private String updatedBy;

    private Long updatedTime;

    private String receiver;

}
