package com.kuailu.im.server.model;


import com.alibaba.druid.support.json.JSONUtils;
import com.kuailu.im.core.ImStatus;
import lombok.Data;

import java.io.Serializable;


public class ResponseModel implements Serializable {
    private static final long serialVersionUID = 1L;
    private String code;
    private String msg;
    private Object data;

    public ResponseModel() {
    }

    public ResponseModel(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public ResponseModel(String code, Object data) {
        this.code = code;
        this.data = data;
    }

    public ResponseModel(String code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static ResponseModel success(Object data) {
        return new ResponseModel(String.valueOf(ImStatus.OK.getCode()), data);
    }
    public static ResponseModel success() {
        return new ResponseModel(String.valueOf(ImStatus.OK.getCode()), "");
    }
  /*  public static ResponseModel error(String msg) {
        return new ResponseModel(ERROR_CODE, msg);
    } */

    public   ResponseModel error(String msg) {
        return new ResponseModel(String.valueOf(ImStatus.ERROR.getCode()), msg);
    }

    public static ResponseModel error() {
        return new ResponseModel(String.valueOf(ImStatus.ERROR.getCode()), "");
    }
    public String toJsonString() {
        return JSONUtils.toJSONString(this);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
