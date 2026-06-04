package com.douyin.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应体
 *
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    /** 业务状态码：200 成功，其他为异常 */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 响应数据，可为 null */
    private T data;

    /**
     * 请求成功（带数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return Result
     */
    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 请求成功（无数据）
     *
     * @param <T> 数据类型
     * @return Result
     */
    public static <T> Result<T> ok() {
        return ok(null);
    }

    /**
     * 请求失败
     *
     * @param code    业务状态码
     * @param message 错误提示
     * @param <T>     数据类型
     * @return Result
     */
    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}
