package com.kuailu.im.server.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 *    * 1 完全空白
 *      * 2，上个话题已关闭
 *      * 3 。上个话题还没有关闭
 */
public enum AITopicStatusEnum {
    TOTAL_EMPTY(1, "完全空白"),
    TOPIC_CLOSED(2, "上个话题已关闭"),
    TOPIC_NOT_CLOSED(3, "上个话题还没有关闭");

    @EnumValue
    @JsonValue
    private final int code;
    private final String info;

    private AITopicStatusEnum(int code, String info) {
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
