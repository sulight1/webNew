package com.example.fingerartbackend.dto;

import lombok.Data;

/**
 * TOTP 登录验证请求 DTO。
 * 管理员两步登录的第二步，提交预认证 Token 与验证码。
 */
@Data
public class TotpVerifyRequest {
    /** 密码验证通过后获得的短期预认证 Token */
    private String preAuthToken;

    /** 6 位 TOTP 动态验证码 */
    private String code;
}
