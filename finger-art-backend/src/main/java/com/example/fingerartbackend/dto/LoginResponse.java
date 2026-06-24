package com.example.fingerartbackend.dto;

import com.example.fingerartbackend.entity.User;
import lombok.Data;

/**
 * 用户登录响应 DTO。
 * 返回 JWT Token 及用户信息；管理员启用 TOTP 时需二次验证。
 */
@Data
public class LoginResponse {
    /** 访问令牌 */
    private String token;

    /** 令牌类型，固定 Bearer */
    private String tokenType = "Bearer";

    /** 令牌有效期（秒） */
    private long expiresIn;

    /** 登录用户信息 */
    private User user;

    /** 管理员已启用 TOTP 时需第二步验证 */
    private boolean requiresTotp;

    /** 密码验证通过后的短期预认证 Token，用于提交 TOTP 验证码 */
    private String preAuthToken;
}
