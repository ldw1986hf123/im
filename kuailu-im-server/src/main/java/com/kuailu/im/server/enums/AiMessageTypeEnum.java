package com.kuailu.im.server.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 0文本，1 文件，2 语音，3 视频
 */
@Deprecated
public enum AiMessageTypeEnum {
    TEXT(0, "文本"),
    FILE(1, "文件"),
    AUDIO(2, "语音"),
    VIDEO(3, "视频"),
    SQL(4, "sql查询"),
    ;

    @EnumValue
    @JsonValue
    private final int code;
    private final String info;

    private AiMessageTypeEnum(int code, String info) {
        this.code = code;
        this.info = info;
    }

    public int getCode() {
        return this.code;
    }

    public String getInfo() {
        return this.info;
    }

    public static String getInfoByType(Integer msgType) {
        for (AiMessageTypeEnum chatGroupType : AiMessageTypeEnum.values()) {
            if (chatGroupType.getCode() ==msgType) {
                return chatGroupType.getInfo();
            }
        }
        return "";
    }

}
