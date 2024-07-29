package com.kuailu.im.server.vo;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @description:
 * @author: liangdl
 * @time: 2023-07-04
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
//@Builder
public class SearchResp implements Serializable {

    private static final long serialVersionUID = 7041132621966527207L;

    List<AddressBookResp> addressBookResps;

    List<ChatGroupResp> chatGroupResps;

    List<Map<String, Object>> chatRecordResps;

}
