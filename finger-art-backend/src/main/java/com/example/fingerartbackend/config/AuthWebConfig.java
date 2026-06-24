package com.example.fingerartbackend.config;

import com.example.fingerartbackend.auth.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 认证拦截器 Web 配置。
 * <p>
 * 将 {@link AuthInterceptor} 注册到全部 API 路径，在 Controller 执行前完成鉴权校验。
 * </p>
 */
@Configuration
public class AuthWebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    /**
     * 新增认证。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor).addPathPatterns("/**");
    }
}
