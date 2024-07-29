package com.kuailu.im.server.req;

import com.kuailu.im.core.packets.Message;
import lombok.Data;

@Data
public class MessageContextReqBody extends Message {
    //early ，later
    private String direction;
    private String messageId;
    /**
     * 群组id;
     */
    private String groupId;
    /**
     * 数量
     */
    private Integer count = 50;

    public static final  String DIRECTION_EARLY="early";
    public static final  String DIRECTION_LATER="later";
}
