package com.example.fingerartbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 图像生成配置属性，绑定 {@code ai.image.*} 配置项。
 * <p>
 * 控制通义万相等图像生成服务的提供商、API 密钥及轮询参数。
 * </p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai.image")
public class AiImageProperties {

    /** dashscope = 阿里云通义万相 */
    private String provider = "dashscope";

    /** 对外访问本地上传文件的基础 URL */
    private String publicBaseUrl = "http://localhost:3000";

    /** DashScope 图像生成子配置 */
    private DashScope dashscope = new DashScope();

    /**
     * 阿里云 DashScope 图像生成 API 配置。
     */
    @Data
    public static class DashScope {
        /** DashScope API Key */
        private String apiKey = "";
        /** 图像生成模型名称 */
        private String model = "wanx2.1-t2i-turbo";
        /** 中国区 API 基础地址 */
        private String baseUrl = "https://dashscope.aliyuncs.com";
        /** 异步任务轮询间隔（毫秒） */
        private int pollIntervalMs = 3000;
        /** 最大轮询次数 */
        private int maxPollAttempts = 40;
    }
}
