package com.example.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultStatus {

    SUCCESS(200, "操作成功"),

    BAD_REQUEST(400, "请求参数错误"),

    UNAUTHORIZED(401, "未登录或 Token 已过期"),

    SERVER_ERROR(500, "服务端异常"),

    OPERATION_FAILED(1001, "操作失败"),
    BAD_CREDENTIALS(1002, "用户名或密码错误"),
    ;

    private final int code;

    private final String msg;
}
