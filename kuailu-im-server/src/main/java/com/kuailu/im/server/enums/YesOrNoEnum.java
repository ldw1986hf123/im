package com.kuailu.im.server.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum YesOrNoEnum {
    YES(1, "是"),
    NO(0, "否");

    @EnumValue
    @JsonValue
    private final int code;
    private final String info;

    private YesOrNoEnum(int code, String info) {
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
