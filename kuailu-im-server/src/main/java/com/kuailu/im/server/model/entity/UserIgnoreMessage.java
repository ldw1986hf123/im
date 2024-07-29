package com.kuailu.im.server.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户屏蔽消息记录表
 * </p>
 *
 * @author linjd
 * @since 2022-12-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("im_user_ignore_message")
public class UserIgnoreMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 账号名
     */
    private String userName;

    /**
     * 会话
     */
    private String conversationId;

    /**
     * 终端编号
     */
    private String deviceId;

    /**
     * 消息ID
     */
    private String messageId;

    private String createdBy;

    private LocalDateTime createdTime;

    private String updatedBy;

    private LocalDateTime updatedTime;


}
