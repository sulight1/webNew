package com.example.fingerartbackend.dto;

import lombok.Data;

/**
 * 用户登录请求 DTO。
 * 用于 POST /api/auth/login 接口提交账号与密码。
 */
@Data
public class LoginRequest {
    /** 数字账号 */
    private String account;

    /** 登录密码 */
    private String password;
}
