package com.kuailu.im.server.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 `status` int DEFAULT '0' COMMENT '0:正常，1:撤回',
 */
public enum MessageStatusEnum {
    OK (0, "正常"),
    REVOKED(1, "撤回") ;

    @EnumValue
    @JsonValue
    private final int code;
    private final String info;

    private MessageStatusEnum(int code, String info) {
        this.code = code;
        this.info = info;
    }

    public int getCode() {
        return this.code;
    }

    public String getInfo() {
        return this.info;
    }
}
