package com.kuailu.im.server.processor.param;

import lombok.Data;

@Data
public class MessageContextParam {
    //gt greater than ；lt lower than
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

}
