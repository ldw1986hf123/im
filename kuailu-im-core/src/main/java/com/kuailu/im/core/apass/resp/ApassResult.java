package com.kuailu.im.core.apass.resp;


import com.kuailu.im.core.apass.resp.status.ApassCode;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

@Data
@Accessors(chain = true)
public class ApassResult {
    private Object data;
    private String msg;
    private String code;

    public ApassResult(ApassCode resultCode, String msg, Object data) {
        this.code = resultCode.getStatus();
        this.msg = msg;
        this.data = data;

    }

    public ApassResult() {
    }

    public void success() {
       this.setCode(ApassCode.OK.getStatus());
    }

    public void success(Object data) {
        this.setCode(ApassCode.OK.getStatus());
        this.setData(data);
    }


 /*   public static ApassResult success(String msg) {
        return new ApassResult(ApassCode.OK, msg, (Object) null);
    }*/

    public static ApassResult fail() {
        return new ApassResult(ApassCode.ERROR, (String) null, (Object) null);
    }

    public void fail(ApassCode apassCode) {
        this.setCode(apassCode.getStatus());
        this.setMsg(apassCode.getText());
    }

    public ApassResult fail(String msg) {
        return this.setMsg(msg);
    }


    public static ApassResult result(ApassCode resultCode, String msg) {
        if (StringUtils.isEmpty(msg)) {
            msg = msg;
        }

        return new ApassResult(resultCode, msg, (Object) null);
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public ApassResult setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
