package com.kuailu.im.server.enums;

/**
 * @description:
 * @author: 林坚丁
 * @time: 2022/12/8 16:31
 */
public enum ChatGroupStatus {
    NORMAL("1", "正常"),
    PROHIBIT("2", "禁言");



    private String code;
    private String cnName;

    ChatGroupStatus(String code, String cnName) {
        this.code = code;
        this.cnName = cnName;
    }
    public static String getCnNameByCode(String code) {
        for (ChatGroupStatus chatGroupType : ChatGroupStatus.values()) {
            if (chatGroupType.getCode().equals(code)) {
                return chatGroupType.getCnName();
            }
        }
        return "";
    }
    public static String getCodeByCnName(String cnName) {
        for (ChatGroupStatus chatGroupType : ChatGroupStatus.values()) {
            if (chatGroupType.getCnName().equals(cnName)) {
                return chatGroupType.getCode();
            }
        }
        return "";
    }
    public String getCode() {
        return code;
    }

    public String getCnName() {
        return cnName;
    }
}
