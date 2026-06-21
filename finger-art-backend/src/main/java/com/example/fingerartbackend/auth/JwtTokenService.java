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

@Service
public class JwtTokenService {

    private final AuthProperties properties;
    private final SecretKey secretKey;

    public JwtTokenService(AuthProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

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

    /** 管理员密码验证通过后、TOTP 验证前的短期 Token */
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
            return new AuthUser(id, username, role, Boolean.TRUE.equals(adminSession));
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public long getExpirationMs(User user) {
        return "ADMIN".equals(user.getRole())
                ? properties.getAdminExpirationMs()
                : properties.getExpirationMs();
    }
}
