package com.kuailu.im.server.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import com.kuailu.im.server.model.entity.BaseEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Data
@Accessors(chain = true)
public class UserCacheDto {
    private String userId;
    /**
     * 用户名称
     */
    private String userName;

    /**
     * 员工编号
     */
    private String userNo;


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


    /**
     * 终端类型：web、app、h5
     */
    private String clientType;

//    private List<GroupCacheDto> groups=new ArrayList();

    /**
     * 在线状态, 0:online, 1:offline
     */
    private Boolean onlineStatus;
    private Integer staffStatus;
    /**
     * 总的消息未读数
     */
    private int totalUnreadCount;

    private String token;

}
