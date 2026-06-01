package com.example.backend.exception;

import lombok.Getter;

/**
 * 业务异常类 - 用于抛出业务逻辑相关的异常
 *
 * 用法：
 * - 在 Service 层遇到业务规则不满足时抛出此异常
 * - 由全局异常处理器（GlobalExceptionHandler）统一捕获并返回给前端
 *
 * 示例：
 *   throw new BusinessException("用户不存在");
 *   throw new BusinessException(400, "参数错误");
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     * 默认 500（系统内部错误），可通过构造函数自定义
     */
    private final int code;

    /**
     * 仅传入错误信息，默认错误码 500
     * @param message 错误提示信息
     */
    public BusinessException(String message) {
        this(500, message);
    }

    /**
     * 传入错误码和错误信息
     * @param code 自定义错误状态码
     * @param message 错误提示信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
