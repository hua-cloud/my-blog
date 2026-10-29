package com.example.trigger.http.advice;

import cn.dev33.satoken.exception.NotLoginException;
import com.example.api.response.R;
import com.example.types.core.AppException;
import com.example.types.enums.ResultStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<Void> handleUnreadableBody(HttpMessageNotReadableException e) {
        log.warn("请求体格式或参数值错误");
        return error("请求体格式或参数取错误");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("方法参数校验失败: {}", msg);
        return error(msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public R<Void> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", msg);
        return error(msg);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public R<Void> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("请求参数不合法: {}", e.getMessage());
        return error(e.getMessage());
    }

    @ExceptionHandler(NotLoginException.class)
    public R<Void> handleNotLogin(NotLoginException e) {
        log.warn("认证失败: {}", e.getMessage());
        return error(ResultStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AppException.class)
    public R<Void> handleBusiness(AppException e) {
        log.warn("业务异常: {}", e.getMessage());
        return error(e.getResultStatus());
    }

    @ExceptionHandler(RuntimeException.class)
    public R<Void> handleRuntime(RuntimeException e) {
        log.error("运行时异常: ", e);
        return error(ResultStatus.SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("未知异常: ", e);
        return error(ResultStatus.SERVER_ERROR);
    }

    private R<Void> error(ResultStatus resultStatus) {
        return new R<>(resultStatus.getCode(), resultStatus.getMsg(), null);
    }

    private R<Void> error(String msg) {
        return new R<>(ResultStatus.BAD_REQUEST.getCode(), msg, null);
    }
}
