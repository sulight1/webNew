package com.example.fingerartbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码编码器配置。
 * <p>
 * 注册 BCrypt 密码哈希 Bean，用于用户注册、登录时的密码加密与校验。
 * </p>
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * 提供 BCrypt 密码编码器实例。
     *
     * @return PasswordEncoder 实现
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
