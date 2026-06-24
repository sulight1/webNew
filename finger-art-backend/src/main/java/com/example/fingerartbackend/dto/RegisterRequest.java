package com.example.fingerartbackend.dto;

import lombok.Data;

/**
 * 用户注册请求 DTO。
 * 用于 POST /api/auth/register 接口创建新账号。
 */
@Data
public class RegisterRequest {
    /** 数字账号，全局唯一 */
    private String account;

    /** 登录密码 */
    private String password;

    /** 确认密码，需与 password 一致 */
    private String confirmPassword;
}
