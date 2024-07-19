package com.kuailu.im.core.exception;

import com.kuailu.im.core.apass.resp.status.ApassCode;
import lombok.Data;

@Data
public class PassException extends RuntimeException {

    private String code;
    private String message;

    public PassException() {
    }

    public PassException(ApassCode apassCode) {

        this.code = apassCode.getStatus();
        this.message=apassCode.getDescription();
    }

}
