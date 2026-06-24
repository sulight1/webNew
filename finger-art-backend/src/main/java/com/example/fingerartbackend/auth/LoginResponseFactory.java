package com.example.fingerartbackend.auth;

import com.example.fingerartbackend.dto.LoginResponse;
import com.example.fingerartbackend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 登录响应构建工厂。
 * <p>
 * 根据登录场景组装 {@link LoginResponse}：
 * 普通用户/已完成 TOTP 的管理员返回正式 Token；
 * 待 TOTP 验证的管理员返回预认证 Token 并标记 {@code requiresTotp=true}。
 * </p>
 */
@Component
public class LoginResponseFactory {

    @Autowired
    private JwtTokenService jwtTokenService;

    /**
     * 构建正式登录响应，包含访问 Token 及过期时间。
     *
     * @param user 已验证用户
     * @return 登录响应
     */
    public LoginResponse build(User user) {
        LoginResponse response = new LoginResponse();
        response.setToken(jwtTokenService.generateToken(user));
        response.setTokenType("Bearer");
        response.setExpiresIn(jwtTokenService.getExpirationMs(user));
        response.setUser(user);
        response.setRequiresTotp(false);
        return response;
    }

    /**
     * 构建管理员 TOTP 预认证响应，引导客户端继续二次验证。
     *
     * @param user 已通过密码验证的管理员
     * @return 含预认证 Token 的登录响应
     */
    public LoginResponse buildRequiresTotp(User user) {
        LoginResponse response = new LoginResponse();
        response.setPreAuthToken(jwtTokenService.generatePreAuthToken(user));
        response.setRequiresTotp(true);
        response.setUser(user);
        return response;
    }
}
