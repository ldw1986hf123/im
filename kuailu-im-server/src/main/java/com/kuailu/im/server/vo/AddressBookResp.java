package com.kuailu.im.server.vo;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressBookResp {

    String userId;

    String userName;

    String ownerOrgName;

    String positionName;

    String avatar;

    String userStatus;

}
