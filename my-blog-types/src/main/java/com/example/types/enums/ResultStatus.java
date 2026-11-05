package com.example.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultStatus {

    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或 Token 已过期"),

    SERVER_ERROR(500, "服务端异常"),

    OPERATION_SUCCESS(1000, "操作成功"),
    OPERATION_FAILED(1001, "操作失败"),

    BAD_CREDENTIALS(2002, "用户名或密码错误"),

    CATEGORY_NOT_FOUND(3001, "分类不存在"),
    CATEGORY_NAME_EXISTS(3002, "分类名称已存在"),

    TAG_NOT_FOUND(4001, "标签不存在"),
    TAG_NAME_EXISTS(4002, "标签名称已存在"),

    FILE_UPLOAD_FAILED(5001, "文件上传失败"),
    FILE_STORAGE_KEY_CONFLICT(5002, "文件存储键已存在"),
    FILE_UPLOAD_RESULT_UNKNOWN(5003, "文件上传结果不确定"),
    FILE_DELETE_FAILED(5004, "文件删除失败"),
    ;

    private final int code;

    private final String msg;
}
