package com.kuailu.im.core.http;


import com.kuailu.im.core.ImStatus;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

@Accessors(chain = true)
public class AjaxResult {
    private Object data;
    private String msg;
    private Integer code;

    public AjaxResult(ImStatus resultCode, String msg, Object data) {
        this.code = resultCode.getCode();
        this.msg = msg;
        this.data = data;

    }

    public AjaxResult() {
    }

    public static AjaxResult success() {
        return new AjaxResult(ImStatus.OK, "ok", "");
    }

    public AjaxResult success(Object data) {
        this.setCode(ImStatus.OK.getCode());
        this.setData(data);
        this.setMsg("ok");
        return this;
    }


    public static AjaxResult success(String msg) {
        return new AjaxResult(ImStatus.OK, msg, (Object) null);
    }

    public static AjaxResult fail() {
        return new AjaxResult(ImStatus.ERROR, (String) null, (Object) null);
    }

    public static AjaxResult fail(ImStatus ImStatus, String msg) {
        return new AjaxResult(ImStatus, msg, "");
    }

    public AjaxResult fail(String msg) {
        return this.setMsg(msg);
    }


    public static AjaxResult result(ImStatus resultCode) {
        return new AjaxResult(resultCode, resultCode.getMsg(), (Object) null);
    }

    public static AjaxResult result(ImStatus resultCode, String msg) {
        if (StringUtils.isEmpty(msg)) {
            msg = resultCode.getMsg();
        }

        return new AjaxResult(resultCode, msg, (Object) null);
    }

 /*   public AjaxResult put(String key, Object value) {
        super.put(key, value);
        return this;
    }*/

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public AjaxResult setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
