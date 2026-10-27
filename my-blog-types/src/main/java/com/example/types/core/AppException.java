package com.example.types.core;

import com.example.types.enums.ResultStatus;
import lombok.Getter;

@Getter
public class AppException extends RuntimeException {

    private final ResultStatus resultStatus;

    public AppException(ResultStatus resultStatus) {
        super(resultStatus.getMsg());
        this.resultStatus = resultStatus;
    }
}
