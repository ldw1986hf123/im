package com.kuailu.im.server.processor.param;

import lombok.Data;

import java.util.List;


@Data
public class AITrampleParam {
    private  String messageId;
    private List<String> feedback;
    private  String comment;
}
