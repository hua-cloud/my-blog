package com.example.trigger.http.advice;

import cn.dev33.satoken.exception.NotLoginException;
import com.example.types.core.AppException;
import com.example.types.core.R;
import com.example.types.enums.ResultStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<Void> handleUnreadableBody(HttpMessageNotReadableException e) {
        log.warn("请求体格式或参数值错误");
        return R.error(ResultStatus.BAD_REQUEST, "请求体格式或参数取错误");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public R<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型错误: {}", e.getName());
        return R.error(ResultStatus.BAD_REQUEST, "参数类型错误: " + e.getName());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("方法参数校验失败: {}", msg);
        return R.error(ResultStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public R<Void> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", msg);
        return R.error(ResultStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public R<Void> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("请求参数不合法: {}", e.getMessage());
        return R.error(ResultStatus.BAD_REQUEST, e.getMessage());
    }

    /**
     * 返回缺少必填请求部分的提示。
     *
     * @param e 请求部分缺失异常
     * @return 参数错误响应
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public R<Void> handleMissingRequestPart(MissingServletRequestPartException e) {
        return R.error(ResultStatus.BAD_REQUEST, "缺少请求部分 " + e.getRequestPartName());
    }

    /**
     * 返回请求内容类型不受支持的提示。
     *
     * @return 参数错误响应
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public R<Void> handleUnsupportedMediaType() {
        return R.error(ResultStatus.BAD_REQUEST, "不支持的请求内容类型");
    }

    /**
     * 将 Servlet multipart 解析阶段的大小超限转换为参数错误。
     *
     * @return 上传大小提示
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public R<Void> handleUploadSizeExceeded() {
        return R.error(ResultStatus.BAD_REQUEST, "上传文件大小或请求总大小超过限制");
    }

    /**
     * 将无法解析的 multipart 请求转换为参数错误。
     *
     * @return 请求格式提示
     */
    @ExceptionHandler(MultipartException.class)
    public R<Void> handleMultipart() {
        return R.error(ResultStatus.BAD_REQUEST, "无法解析 multipart 上传请求");
    }

    @ExceptionHandler(NotLoginException.class)
    public R<Void> handleNotLogin(NotLoginException e) {
        log.warn("认证失败: {}", e.getMessage());
        return R.error(ResultStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AppException.class)
    public R<Void> handleBusiness(AppException e) {
        log.warn("业务异常: {}", e.getMessage());
        return R.error(e.getResultStatus());
    }

    @ExceptionHandler(RuntimeException.class)
    public R<Void> handleRuntime(RuntimeException e) {
        log.error("运行时异常: ", e);
        return R.error(ResultStatus.SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("未知异常: ", e);
        return R.error(ResultStatus.SERVER_ERROR);
    }
}
