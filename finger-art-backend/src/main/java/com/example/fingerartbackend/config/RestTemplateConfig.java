package com.example.fingerartbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP 客户端配置。
 * <p>
 * 提供 {@link RestTemplate} Bean，供 AI 图像生成、物流查询等外部 HTTP 调用使用。
 * </p>
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 创建默认 RestTemplate 实例。
     *
     * @return RestTemplate
     */
    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
