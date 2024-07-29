package com.kuailu.im.server.mq;

import com.kuailu.im.core.packets.Message;

public class IatParam extends Message {
    String userId;
    private String bytes;
    private String serial;

    // start end
    private String status;

    public IatParam() {
    }

    public String getBytes() {
        return bytes;
    }

    public void setBytes(String bytes) {
        this.bytes = bytes;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
