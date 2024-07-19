package com.kuailu.im.server.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户设备记录表
 * </p>
 *
 * @author linjd
 * @since 2022-12-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("im_user_device")
public class UserDevice implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 业务
     */
    private String appKey;

    /**
     * 账号名
     */
    private String userName;

    /**
     * 终端编号
     */
    private String deviceId;

    /**
     * 会话
     */
    private String conversationId;

    /**
     * 第一次使用时间
     */
    private Long readMessageTimeMs;

    /**
     * 第一次使用时间
     */
    private Long firstMessageTimeMs;

    /**
     * 是否临时屏蔽
     */
    private Boolean isIgnore;

    private String createdBy;

    private LocalDateTime createdTime;

    private String updatedBy;

    private LocalDateTime updatedTime;


}
