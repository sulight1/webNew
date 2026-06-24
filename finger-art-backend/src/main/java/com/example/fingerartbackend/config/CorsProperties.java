package com.example.fingerartbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * CORS 跨域配置属性，绑定 {@code app.cors.*} 配置项。
 * <p>
 * 定义允许访问后端 API 的前端 Origin 白名单。
 * </p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    /** 允许的前端 Origin，生产环境改为实际域名 */
    private List<String> allowedOrigins = List.of(
            "http://localhost:5173",
            "http://localhost:5174",
            "http://127.0.0.1:5173",
            "http://127.0.0.1:5174"
    );
}
