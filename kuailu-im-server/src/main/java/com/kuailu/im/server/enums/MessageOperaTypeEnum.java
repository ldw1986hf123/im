package com.kuailu.im.server.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 */
public enum MessageOperaTypeEnum {
    MergeRedirect ("mergeRedirect", "合并转发"),
    RedirectMerge("redirectMerge", "转发合并"), ;

    @EnumValue
    @JsonValue
    private final String code;
    private final String info;

    private MessageOperaTypeEnum(String code, String info) {
        this.code = code;
        this.info = info;
    }

    public String getCode() {
        return this.code;
    }

    public String getInfo() {
        return this.info;
    }
}
