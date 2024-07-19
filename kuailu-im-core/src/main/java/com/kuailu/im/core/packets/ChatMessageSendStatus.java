package com.kuailu.im.core.packets;

import java.util.Arrays;
import java.util.List;

/**
 * @description:
 * @author: 林坚丁
 * @time: 2023/2/14 14:18
 */
public enum ChatMessageSendStatus {

    PENDING(0, "发送未开始"),

    DELIVERING(1, "正在发送"),

    SUCCEED(2,  "发送成功"),
    FAILED(3, "发送失败")
    ;


    private Integer value;
    private String cnName;

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getCnName() {
        return cnName;
    }

    public void setCnName(String cnName) {
        this.cnName = cnName;
    }

    ChatMessageSendStatus(Integer value, String cnName) {
        this.value = value;
        this.cnName = cnName;
    }

    public static String getCnNameByCode(Integer code) {
        for (ChatMessageSendStatus yesOrNo : ChatMessageSendStatus.values()) {
            if (yesOrNo.getValue()==code) {
                return yesOrNo.getCnName();
            }
        }
        return "";
    }

    public static Integer getCodeByCnName(String cnName) {
        for (ChatMessageSendStatus yesOrNo : ChatMessageSendStatus.values()) {
            if (yesOrNo.getCnName().equals(cnName)) {
                return yesOrNo.getValue();
            }
        }
        return null;
    }
    public static boolean isExistValue(Integer code) {
        for (ChatMessageSendStatus us : ChatMessageSendStatus.values()) {
            if (us.getValue()==code) {
                return true;
            }
        }
        return false;
    }
    public static ChatMessageSendStatus parse(Integer code) {
        for (ChatMessageSendStatus us : ChatMessageSendStatus.values()) {
            if (us.getValue()==code) {
                return us;
            }
        }
        return null;
    }

    public static List<ChatMessageSendStatus> getAllList(){
        return Arrays.asList(ChatMessageSendStatus.values());
    }


}
