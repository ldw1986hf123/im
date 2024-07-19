package com.kuailu.im.server.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.kuailu.im.server.enums.YesOrNoEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 *
 */
@Accessors(chain = true)
@TableName("im_at_msg")
@Data
@NoArgsConstructor
public class AtMsg extends BaseEntity implements Serializable{

    /**
     * 消息ID
     */
    private String msgId;

    private String atUser;


    private String conversationId;

    private String groupId;

    public AtMsg(String msgId, String atUser, String groupId, String conversationId) {
        this.msgId = msgId;
        this.conversationId = conversationId;
        this.atUser = atUser;
        this.groupId = groupId;
        this.isRead = YesOrNoEnum.NO.getCode();
    }

    /**
     * 0:未读，1:已读
     */
    private Integer isRead;

}
