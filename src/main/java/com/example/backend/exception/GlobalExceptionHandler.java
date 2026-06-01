package com.example.backend.exception;

import com.example.backend.common.R;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 全局异常处理器 - 统一处理各类异常，返回标准响应格式
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 业务异常处理 - 根据错误码返回对应的HTTP状态码
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<R<?>> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        // 根据code映射HTTP状态码：401未认证、403无权限、400参数错误、其他返回200
        HttpStatus status = switch (e.getCode()) {
            case 401 -> HttpStatus.UNAUTHORIZED;
            case 403 -> HttpStatus.FORBIDDEN;
            case 400 -> HttpStatus.BAD_REQUEST;
            default  -> HttpStatus.OK;
        };
        return ResponseEntity.status(status).body(R.fail(e.getCode(), e.getMessage()));
    }

    // @Valid注解校验异常 - 处理方法参数校验失败（如@RequestBody参数）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> handleValidationException(MethodArgumentNotValidException e) {
        // 提取第一个字段校验错误信息
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("参数校验失败");
        return R.fail(400, message);
    }

    // 表单绑定异常处理 - 处理GET请求参数绑定校验失败
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("参数绑定失败");
        return R.fail(400, message);
    }

    // 约束违反异常处理 - 处理方法级别校验约束失败
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> handleConstraintViolationException(ConstraintViolationException e) {
        return R.fail(400, e.getMessage());
    }

    // 兜底异常处理 - 捕获所有未处理异常，返回500内部错误
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<?> handleException(Exception e) {
        log.error("系统异常: ", e);
        return R.fail(500, "系统内部错误");
    }
}
