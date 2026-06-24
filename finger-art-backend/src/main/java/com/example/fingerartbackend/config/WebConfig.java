package com.example.fingerartbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC 静态资源配置。
 * <p>
 * 将 {@code /uploads/**} 请求映射到应用工作目录下的本地 {@code uploads} 文件夹，
 * 用于访问用户上传的图片等静态资源。
 * </p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 新增Web。
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = "file:" + System.getProperty("user.dir") + File.separator + "uploads" + File.separator;
        // 将 /uploads/** 的请求映射到本地物理路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}
