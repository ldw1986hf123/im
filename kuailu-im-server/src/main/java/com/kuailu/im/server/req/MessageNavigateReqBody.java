package com.kuailu.im.server.req;

import com.kuailu.im.core.packets.Message;
import lombok.Data;

@Data
public class MessageNavigateReqBody extends Message {
    private String messageId;
    /**
     * 群组id;
     */
    private String groupId;
    /**
     * 数量
     */
    private Integer count = 50;

}
