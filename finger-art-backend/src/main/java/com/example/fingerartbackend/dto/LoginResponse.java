package com.example.fingerartbackend.dto;

import com.example.fingerartbackend.entity.User;
import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";
    private long expiresIn;
    private User user;
    /** 管理员已启用 TOTP 时需第二步验证 */
    private boolean requiresTotp;
    /** 密码验证通过后的短期预认证 Token，用于提交 TOTP 验证码 */
    private String preAuthToken;
}
