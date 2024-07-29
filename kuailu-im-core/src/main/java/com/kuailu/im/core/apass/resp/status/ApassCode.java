/**
 *
 */
package com.kuailu.im.core.apass.resp.status;

import lombok.Data;

public enum ApassCode {

    OK("200", "ok", "成功"),
    ERROR("-1", "Unknow exception!", "系统繁忙，请稍后再试"),
    NEED_LOGIN("-2", "未登录", "请先登录"),

    ILLEGAL_PARAM("-3", "参数不合法", "参数不合法"),
    ;


    private String status;

    private String description;

    private String text;

    ApassCode(String status, String description, String text) {
        this.status = status;
        this.description = description;
        this.text = text;
    }

    public String getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public String getText() {
        return text;
    }


}
