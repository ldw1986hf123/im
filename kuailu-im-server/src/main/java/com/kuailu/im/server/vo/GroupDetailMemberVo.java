package com.kuailu.im.server.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Accessors(chain = true)
public class GroupDetailMemberVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;


    /**
     * 群编号
     */
    private String groupId;

    /**
     * 账号名
     */
    private String userName;
    /**
     * 用户编号
     */
    private  String userNo;

    private String userId;
    /**
     * 成员角色, OWNER, ADMIN, MEMBER
     */
    private String roleType;

    private String createdBy;

    private Date createdTime;

    private String updatedBy;

    private LocalDateTime updatedTime;
    /** 员工状况 1-在线,2-离线,3-请假,4-外出,5-出差 */
    private Integer staffStatus;
    private String staffStatusName;
    private String statusDesc;
}
