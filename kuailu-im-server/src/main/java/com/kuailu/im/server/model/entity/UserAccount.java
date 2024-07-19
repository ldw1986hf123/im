package com.kuailu.im.server.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 */
@Data
@Accessors(chain = true)
@TableName("im_user_account")
public class UserAccount extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 应用Key
     */
    private String appKey;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 员工编号
     */
    private String userNo;
    /**
     * 密码
     */
    private String password;

    /**
     * 账号状态, 0:封禁, 1:正常, 2:禁言, 3:不可用
     */
    private Integer status;

    /**
     * 头像地址
     */
    private String avatarUrl;

    /**
     * 别名
     */
    private String nickName;

    /**
     * 设备号
     */
    private String deviceId;

    private String  userId;
    private String  seid;
    /**
     * 终端类型：web、app、h5
     */
    private String clientType;

    /**
     * 在线状态, 0:online, 1:offline
     */
    private Boolean onlineStatus;

    /**
     * 禁言开始时间
     */
    private Date startMuteTime;

    /**
     * 禁言多长时间，单位秒，-1:表示永久
     */
    private Long muteDuration;


    private Date lastLoginTime;
    private Date lastLogoutTime;

    private Integer staffStatus;
}
