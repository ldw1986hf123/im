/**
 *
 */
package com.kuailu.im.core.packets;

import java.io.Serializable;
import java.util.Objects;

import com.kuailu.im.core.Status;
import com.kuailu.im.core.utils.JsonKit;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 *
 */
@Data
@Accessors(chain = true)
public class RespBody implements Serializable {

    protected static final long serialVersionUID = 1L;
    /**
     * 响应状态码;
     */
    protected Integer code;
    /**
     * 响应状态信息提示;
     */
    protected String msg;
    /**
     * 响应cmd命令码;
     */
    protected Command cmd;
    /**
     * 响应数据;
     */
    protected Object data;

    public RespBody() {
    }

    public RespBody(Object o) {
        this.data = o;
    }

    public RespBody(Command cmd) {
        this.cmd = cmd;
    }

    public RespBody(Integer cmd) {
        this.cmd = Command.forNumber(cmd);
    }

    public RespBody(Command cmd, Object data) {
        this(cmd);
        this.data = data;
    }

    public RespBody(Integer cmd, Object data) {
        this(Command.forNumber(cmd));
        this.data = data;
    }

    public RespBody(Command cmd, Status status) {
        this(status);
        this.cmd = cmd;
    }

    public RespBody(Command cmd, Status status, Object data) {
        this(status);
        this.cmd = cmd;
        this.data = data;
    }

    public RespBody(Status status) {
        if (Objects.nonNull(status)) {
            this.code = status.getCode();
            this.msg = status.getMsg();
        }
    }

    public RespBody(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public String toString() {
        return JsonKit.toJSONEnumNoUsingName(this);
    }

    public byte[] toByte() {
        return JsonKit.toJSONBytesEnumNoUsingName(this);
    }

}
