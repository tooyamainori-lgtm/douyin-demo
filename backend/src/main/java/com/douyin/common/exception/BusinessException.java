package com.douyin.common.exception;

import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 业务状态码 */
    private final Integer code;

    /**
     * @param code    业务状态码
     * @param message 错误提示
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * @param message 错误提示（默认状态码 400）
     */
    public BusinessException(String message) {
        this(400, message);
    }
}
