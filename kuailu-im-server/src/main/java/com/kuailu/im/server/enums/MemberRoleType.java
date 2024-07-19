package com.kuailu.im.server.enums;

/**
 * @description:
 * @author: 林坚丁
 * @time: 2022/12/8 17:25
 */
public enum MemberRoleType {
    OWNER("owner", "所有者"),
    MEMBER("member", "成员"),
    ADMIN("admin", "管理员");



    private String code;
    private String cnName;

    MemberRoleType(String code, String cnName) {
        this.code = code;
        this.cnName = cnName;
    }
    public static String getCnNameByCode(String code) {
        for (MemberRoleType chatGroupType : MemberRoleType.values()) {
            if (chatGroupType.getCode().equals(code)) {
                return chatGroupType.getCnName();
            }
        }
        return "";
    }
    public static String getCodeByCnName(String cnName) {
        for (MemberRoleType chatGroupType : MemberRoleType.values()) {
            if (chatGroupType.getCnName().equals(cnName)) {
                return chatGroupType.getCode();
            }
        }
        return "";
    }
    public static  MemberRoleType getMemberRoleType(String groupOwner, String userId) {
        if (groupOwner.equals(userId)) {
            return MemberRoleType.OWNER;
        }
        return MemberRoleType.MEMBER;
    }
    public String getCode() {
        return code;
    }

    public String getCnName() {
        return cnName;
    }
}
