package com.example.fingerartbackend;

import com.example.fingerartbackend.config.CorsProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 指尖艺术平台后端主启动类。
 * <p>
 * 负责启动 Spring Boot 应用、启用定时任务，并注册全局 CORS 跨域配置。
 * </p>
 */
@SpringBootApplication
@EnableScheduling
public class FingerArtBackendApplication {

    /**
     * 应用入口，启动 Spring Boot 容器。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(FingerArtBackendApplication.class, args);
    }

    /**
     * 注册 CORS 跨域配置，允许前端按 {@link CorsProperties} 中配置的 Origin 访问 API。
     *
     * @param corsProperties CORS 配置属性
     * @return WebMvc 跨域配置器
     */
    @Bean
    public WebMvcConfigurer corsConfigurer(CorsProperties corsProperties) {
        return new WebMvcConfigurer() {
            /** 配置允许跨域访问的路径、来源与方法。 */
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                String[] origins = corsProperties.getAllowedOrigins().toArray(String[]::new);
                registry.addMapping("/**")
                        .allowedOriginPatterns(origins)
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}
