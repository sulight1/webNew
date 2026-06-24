package com.example.fingerartbackend.dto;

import lombok.Data;

/**
 * TOTP 启用状态响应 DTO。
 * 查询当前管理员是否已绑定二次验证。
 */
@Data
public class TotpStatusResponse {
    /** 是否已启用 TOTP */
    private boolean enabled;
}
