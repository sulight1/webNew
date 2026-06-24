package com.example.fingerartbackend.dto;

import lombok.Data;

/**
 * 启用 TOTP 请求 DTO。
 * 管理员绑定二次验证时提交密钥与验证码确认。
 */
@Data
public class TotpEnableRequest {
    /** 服务端下发的 TOTP 密钥（Base32） */
    private String secret;

    /** 验证器 App 生成的 6 位验证码 */
    private String code;
}
