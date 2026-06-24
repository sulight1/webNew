package com.example.fingerartbackend.common;

import lombok.Data;

/**
 * 统一 API 响应包装类。
 * <p>
 * 所有 Controller 接口均通过此类返回 JSON，包含状态码、消息及业务数据。
 * </p>
 *
 * @param <T> 响应数据类型
 */
@Data
public class Result<T> {

    /** HTTP 风格状态码，200 表示成功 */
    private Integer code;

    /** 响应消息，成功时为 "success" */
    private String message;

    /** 业务数据载荷 */
    private T data;

    /**
     * 构建成功响应。
     *
     * @param data 业务数据
     * @param <T>  数据类型
     * @return 成功 Result
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    /**
     * 构建失败响应，默认状态码 500。
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 失败 Result
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    /**
     * 构建失败响应，指定状态码。
     *
     * @param code    错误状态码（如 401、403）
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 失败 Result
     */
    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
