package com.example.fingerartbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "auth.jwt")
public class AuthProperties {

    /** 是否强制校验 JWT（生产环境应为 true） */
    private boolean enforce = true;

    /** HS256 密钥，生产环境务必通过环境变量 JWT_SECRET 注入 */
    private String secret = "finger-art-dev-secret-change-in-production-min-32-chars";

    /** Token 有效期（毫秒），默认 7 天 */
    private long expirationMs = 7L * 24 * 60 * 60 * 1000;

    /** 管理员 Token 有效期（毫秒），默认 2 小时 */
    private long adminExpirationMs = 2L * 60 * 60 * 1000;

    /** 管理员 TOTP 预认证 Token 有效期（毫秒），默认 5 分钟 */
    private long preAuthExpirationMs = 5L * 60 * 1000;
}
