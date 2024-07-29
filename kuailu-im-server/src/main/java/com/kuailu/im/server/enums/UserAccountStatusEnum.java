package com.kuailu.im.server.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 账号状态, 0:封禁, 1:正常, 2:禁言, 3:不可用
 */
public enum UserAccountStatusEnum {
    FORBIDDEN(0, "封禁"),
    OK (1, "正常"),
    SHUTUP(2, "禁言"),
    UNAVAILABLE(3, "不可用");

    @EnumValue
    @JsonValue
    private final int code;
    private final String info;

    private UserAccountStatusEnum(int code, String info) {
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
