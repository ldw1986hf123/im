package com.kuailu.im.server.vo;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatGroupResp {

    String groupId;

    String groupName;

    String avatar;

    String searchDesc;

}
