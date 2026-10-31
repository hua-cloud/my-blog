package com.example.types.core;

import com.example.types.enums.ResultStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

import static com.example.types.enums.ResultStatus.OPERATION_FAILED;
import static com.example.types.enums.ResultStatus.OPERATION_SUCCESS;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class R<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int code;

    private String msg;

    private T data;

    public static <T> R<T> success() {
        return new R<>(OPERATION_SUCCESS.getCode(), OPERATION_SUCCESS.getMsg(), null);
    }

    public static <T> R<T> success(String msg) {
        return new R<>(OPERATION_SUCCESS.getCode(), msg, null);
    }

    public static <T> R<T> success(T data) {
        return new R<>(OPERATION_SUCCESS.getCode(), OPERATION_SUCCESS.getMsg(), data);
    }

    public static <T> R<T> success(String msg, T data) {
        return new R<>(OPERATION_SUCCESS.getCode(), msg, data);
    }

    public static <T> R<T> error() {
        return new R<>(OPERATION_FAILED.getCode(), OPERATION_FAILED.getMsg(), null);
    }

    public static <T> R<T> error(ResultStatus resultStatus) {
        return new R<>(resultStatus.getCode(), resultStatus.getMsg(), null);
    }

    public static <T> R<T> error(ResultStatus resultStatus, String msg) {
        return new R<>(resultStatus.getCode(), msg, null);
    }

    public static <T> R<T> operation(boolean flag) {
        return flag ? success() : error();
    }
}
