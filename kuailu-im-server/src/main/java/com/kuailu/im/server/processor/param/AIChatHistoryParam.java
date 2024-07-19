package com.kuailu.im.server.processor.param;

import lombok.Data;


@Data
public class AIChatHistoryParam {
    private  String userId;
    private  String messageId;
    private  String topicId;
    private  int count=10;
}
