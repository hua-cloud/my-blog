package com.example.trigger.http;

import com.example.api.response.R;
import com.example.types.enums.ResultStatus;

import static com.example.types.enums.ResultStatus.OPERATION_FAILED;
import static com.example.types.enums.ResultStatus.SUCCESS;

public class BaseController {

    protected final <T> R<T> success() {
        return new R<>(SUCCESS.getCode(), SUCCESS.getMsg(), null);
    }

    protected final <T> R<T> success(String msg) {
        return new R<>(SUCCESS.getCode(), msg, null);
    }

    protected final  <T> R<T> success(T data) {
        return new R<>(SUCCESS.getCode(), SUCCESS.getMsg(), data);
    }

    protected final  <T> R<T> success(String msg, T data) {
        return new R<>(SUCCESS.getCode(), msg, data);
    }

    protected final <T> R<T> error() {
        return new R<>(OPERATION_FAILED.getCode(), OPERATION_FAILED.getMsg(), null);
    }

    protected final <T> R<T> error(ResultStatus resultStatus) {
        return new R<>(resultStatus.getCode(), resultStatus.getMsg(), null);
    }

    protected final <T> R<T> operation(boolean flag) {
        return flag ? success() : error();
    }
}
