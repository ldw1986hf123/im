package com.kuailu.im.server.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 */
@Data
@Accessors(chain = true)
@TableName("im_chat_unread_msg")
public class ChatUnreadMsg extends BaseEntity {

    /**
     * 消息编号, 非消息表的主键
     */
    private String msgId;

    /**
     * 群编号
     */
    private String groupId;

    /**
     * 账号id
     */
    private String userId;


}
