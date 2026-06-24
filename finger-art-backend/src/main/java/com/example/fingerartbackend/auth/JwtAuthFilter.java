package com.example.fingerartbackend.auth;

import com.example.fingerartbackend.config.AuthProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器，位于 Servlet 过滤器链最前端。
 * <p>
 * 从 {@code Authorization: Bearer <token>} 请求头解析 JWT，
 * 将非预认证用户写入 {@link AuthContext}，请求结束后自动清理上下文。
 * 预认证 Token 不会写入上下文，避免未完成 TOTP 的管理员访问受保护资源。
 * </p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenService jwtTokenService;

    /**
     * 解析 JWT 并设置认证上下文，随后放行请求链。
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7).trim();
                if (!token.isEmpty()) {
                    AuthUser user = jwtTokenService.parseToken(token);
                    if (user != null && !user.preAuth()) {
                        AuthContext.set(user);
                    }
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            AuthContext.clear();
        }
    }
}
