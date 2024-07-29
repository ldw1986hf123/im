package com.kuailu.im.core.exception;

/**
 * @ClassName ImException
 * @Description Im异常类
 * @Author linjd
 * @Date 2022/10/13 3:28
 * @Version 1.0
 **/
public class AppException extends RuntimeException{

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    private int code;

    /**
     * @Author linjd
     * @Description //TODO
     * @param
     * @return
     **/
    public AppException() {
    }

    public AppException(int errorCode) {
        this.code=errorCode;
    }

    /**
     * @Author linjd
     * @Description //TODO
     * @param message
     * @return
     **/
    public AppException(int errorCode,String message) {
        super(message);
        this.code=errorCode;

    }

    /**
     * @Author linjd
     * @Description //TODO
     * @param message, cause
     * @return
     **/
    public AppException(int errorCode,String message, Throwable cause) {
        super(message, cause);
        this.code=errorCode;

    }

    /**
     * @Author linjd
     * @Description //TODO
     * @param message, cause, enableSuppression, writableStackTrace
     * @return
     **/
    public AppException(int errorCode,String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.code=errorCode;
    }

    /**
     * @Author linjd
     * @Description //TODO
     * @param cause
     * @return
     **/
    public AppException(int errorCode,Throwable cause) {
        super(cause);
        this.code=errorCode;
    }
}
