package com.example.fingerartbackend.auth;

import com.example.fingerartbackend.config.AuthProperties;
import com.example.fingerartbackend.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 令牌服务。
 * <p>
 * 负责登录成功后签发 Token、解析请求中的 Bearer Token，
 * 以及管理员双因素认证流程中的预认证 Token 管理。
 * </p>
 */
@Service
public class JwtTokenService {

    private final AuthProperties properties;
    private final SecretKey secretKey;

    public JwtTokenService(AuthProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 为已验证用户生成正式访问 Token。
     * 管理员会话会附加 {@code adminSession} 声明并采用更短的有效期。
     *
     * @param user 登录用户
     * @return JWT 字符串
     */
    public String generateToken(User user) {
        boolean adminSession = "ADMIN".equals(user.getRole());
        long ttl = adminSession ? properties.getAdminExpirationMs() : properties.getExpirationMs();
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ttl);
        var builder = Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("username", user.getUsername())
                .claim("role", user.getRole())
                .issuedAt(now)
                .expiration(expiry);
        if (adminSession) {
            builder.claim("adminSession", true);
        }
        return builder.signWith(secretKey).compact();
    }

    /**
     * 生成管理员 TOTP 预认证 Token。
     * 密码验证通过后、TOTP 验证前签发，有效期较短，不可访问受保护接口。
     *
     * @param user 管理员用户
     * @return 预认证 JWT 字符串
     */
    public String generatePreAuthToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + properties.getPreAuthExpirationMs());
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("username", user.getUsername())
                .claim("role", user.getRole())
                .claim("preAuth", true)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 解析预认证 Token 并返回管理员用户 ID。
     * Token 无效、非预认证或非管理员角色时返回 {@code null}。
     *
     * @param token 预认证 JWT
     * @return 管理员用户 ID，解析失败返回 null
     */
    public Long parsePreAuthUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            if (!Boolean.TRUE.equals(claims.get("preAuth", Boolean.class))) {
                return null;
            }
            if (!"ADMIN".equals(claims.get("role", String.class))) {
                return null;
            }
            return Long.valueOf(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 解析 Bearer Token 为 {@link AuthUser}。
     * 签名无效或已过期时返回 {@code null}。
     *
     * @param token JWT 字符串
     * @return 认证用户快照，解析失败返回 null
     */
    public AuthUser parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Long id = Long.valueOf(claims.getSubject());
            String username = claims.get("username", String.class);
            String role = claims.get("role", String.class);
            Boolean adminSession = claims.get("adminSession", Boolean.class);
            Boolean preAuth = claims.get("preAuth", Boolean.class);
            return new AuthUser(id, username, role, Boolean.TRUE.equals(adminSession), Boolean.TRUE.equals(preAuth));
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 获取用户对应 Token 的有效期（毫秒）。
     *
     * @param user 用户
     * @return Token TTL
     */
    public long getExpirationMs(User user) {
        return "ADMIN".equals(user.getRole())
                ? properties.getAdminExpirationMs()
                : properties.getExpirationMs();
    }
}
