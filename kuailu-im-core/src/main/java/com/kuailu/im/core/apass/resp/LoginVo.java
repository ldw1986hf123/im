package com.kuailu.im.core.apass.resp;

import lombok.Data;

@Data
public class LoginVo {
    private Long id;
    private String receiver;
    private String conversationId;
    private String chatgroupId;
    private Integer chatType;
    private String conversationName;
    private String token;
}
