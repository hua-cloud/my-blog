package com.example.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum ResponseCode {

    SUCCESS(200, "成功"),

    ERROR(0, "未知失败"),

    ILLEGAL_PARAMETER(1001, "非法参数"),
    ;

    private int code;

    private String msg;
}
