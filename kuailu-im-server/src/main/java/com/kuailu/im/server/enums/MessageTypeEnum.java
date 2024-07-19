package com.kuailu.im.server.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * msgType：0:文本、1:文件、2:语音、3:视频、 4：合并转发消息、5、图片 6 @消息,TIPS 提示性消息，
 */
public enum MessageTypeEnum {
    TEXT(0, "文本"),
    FILE(1, "文件"),
    AUDIO(2, "语音"),
    VIDEO(3, "视频"),
    MERGE_REDIRECT(4, "合并转发消息"),
    PICTURE(5, "图片"),
    ATMessage(6, "@消息"),
    TIPS(7, ""),  //提示性消息
    ;

    @EnumValue
    @JsonValue
    private final int code;
    private final String info;

    private MessageTypeEnum(int code, String info) {
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
        for (MessageTypeEnum chatGroupType : MessageTypeEnum.values()) {
            if (chatGroupType.getCode() ==msgType) {
                return chatGroupType.getInfo();
            }
        }
        return "";
    }

}
