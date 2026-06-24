package com.example.fingerartbackend.auth;

/**
 * 已认证用户快照，从 JWT Claims 解析得到。
 *
 * @param id           用户 ID
 * @param username     用户名
 * @param role         角色（如 USER、ADMIN）
 * @param adminSession 是否为已完成 TOTP 验证的管理员正式会话
 * @param preAuth      是否为管理员 TOTP 验证前的预认证 Token
 */
public record AuthUser(Long id, String username, String role, boolean adminSession, boolean preAuth) {
}
