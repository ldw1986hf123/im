package com.kuailu.im.server.processor.param;

import lombok.Data;


@Data
public class PraiseParam {
    private  String messageId;

    //1 赞，2 踩  0 无状态
    private  Integer operation;

    String comment;
}
