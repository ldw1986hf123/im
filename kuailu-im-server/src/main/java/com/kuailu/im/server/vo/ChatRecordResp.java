package com.kuailu.im.server.vo;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatRecordResp {

    String groupId;

    String name;

    int chatType;

    int searchCount;

    String searchDesc;

}
