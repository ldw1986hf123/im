package com.kuailu.im.server.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AIAnswerStatusEnum {
    OK(1, "OK"),
    FAIL(2, "FAIl");

    @EnumValue
    @JsonValue
    private final int code;
    private final String info;

    private AIAnswerStatusEnum(int code, String info) {
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
