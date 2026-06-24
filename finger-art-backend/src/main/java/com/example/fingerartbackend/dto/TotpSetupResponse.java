package com.example.fingerartbackend.dto;

import lombok.Data;

/**
 * TOTP 绑定初始化响应 DTO。
 * 返回密钥、otpauth URL 及二维码，供管理员扫码绑定验证器。
 */
@Data
public class TotpSetupResponse {
    /** TOTP 密钥（Base32） */
    private String secret;

    /** otpauth:// 协议 URL，供验证器 App 识别 */
    private String otpAuthUrl;

    /** 二维码 Data URI，可直接嵌入 img 标签 */
    private String qrCodeDataUri;
}
