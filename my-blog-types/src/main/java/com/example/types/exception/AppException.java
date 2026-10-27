package com.example.types.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AppException extends RuntimeException {

    /** 异常码 */
    private int code;

    /** 异常信息 */
    private String msg;

    public AppException(int code) {
        this.code = code;
    }

    public AppException(int code, Throwable cause) {
        this.code = code;
        super.initCause(cause);
    }

    public AppException(int code, String message) {
        this.code = code;
        this.msg = message;
    }

    public AppException(int code, String message, Throwable cause) {
        this.code = code;
        this.msg = message;
        super.initCause(cause);
    }

    @Override
    public String toString() {
        return "com.example.x.api.types.exception.XApiException{" +
                "code='" + code + '\'' +
                ", msg='" + msg + '\'' +
                '}';
    }
}
