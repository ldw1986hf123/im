package com.kuailu.im.core.packets;

import java.util.Arrays;
import java.util.List;

/**
 * @description:
 * @author: 林坚丁
 * @time: 2023/2/14 14:18
 */
public enum ValidStatus {

    NORMAL(0, "正常"),

    DELETE(1, "删除"),


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

    ValidStatus(Integer value, String cnName) {
        this.value = value;
        this.cnName = cnName;
    }

    public static String getCnNameByCode(Integer code) {
        for (ValidStatus yesOrNo : ValidStatus.values()) {
            if (yesOrNo.getValue()==code) {
                return yesOrNo.getCnName();
            }
        }
        return "";
    }

    public static Integer getCodeByCnName(String cnName) {
        for (ValidStatus yesOrNo : ValidStatus.values()) {
            if (yesOrNo.getCnName().equals(cnName)) {
                return yesOrNo.getValue();
            }
        }
        return null;
    }
    public static boolean isExistValue(Integer code) {
        for (ValidStatus us : ValidStatus.values()) {
            if (us.getValue()==code) {
                return true;
            }
        }
        return false;
    }
    public static ValidStatus parse(Integer code) {
        for (ValidStatus us : ValidStatus.values()) {
            if (us.getValue()==code) {
                return us;
            }
        }
        return null;
    }

    public static List<ValidStatus> getAllList(){
        return Arrays.asList(ValidStatus.values());
    }


}
