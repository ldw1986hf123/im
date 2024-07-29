package com.kuailu.im.server.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AIChatTypeEnum {
//    1是 question；2 是answer。多个answer可能会对应一个question。多个answer可以合成一个卡片
    QUESTION(1, "问题"),
    ANSWER(2, "答案");

    @EnumValue
    @JsonValue
    private final int code;
    private final String info;

    private AIChatTypeEnum(int code, String info) {
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
