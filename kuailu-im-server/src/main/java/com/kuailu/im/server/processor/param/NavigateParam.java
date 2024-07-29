package com.kuailu.im.server.processor.param;

import lombok.Data;


@Data
public class NavigateParam {
    private  String messageId;
    private  String groupId;
    private  int count=50;
}
