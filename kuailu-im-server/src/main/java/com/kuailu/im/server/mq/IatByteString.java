package com.kuailu.im.server.mq;

import lombok.Data;

import java.util.Date;

@Data
public class IatByteString {
    public String userId;
    String bytesStr  ;
}
