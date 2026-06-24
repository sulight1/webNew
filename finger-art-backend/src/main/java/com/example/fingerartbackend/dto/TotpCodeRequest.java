package com.example.fingerartbackend.dto;

import lombok.Data;

/**
 * TOTP 验证码请求 DTO。
 * 用于提交 6 位动态验证码（启用或验证场景）。
 */
@Data
public class TotpCodeRequest {
    /** 6 位 TOTP 动态验证码 */
    private String code;
}
