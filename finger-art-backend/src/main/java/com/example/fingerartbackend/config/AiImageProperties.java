package com.example.fingerartbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.image")
public class AiImageProperties {

    /** dashscope = 阿里云通义万相 */
    private String provider = "dashscope";

    /** 对外访问本地上传文件的基础 URL */
    private String publicBaseUrl = "http://localhost:3000";

    private DashScope dashscope = new DashScope();

    @Data
    public static class DashScope {
        private String apiKey = "";
        private String model = "wanx2.1-t2i-turbo";
        /** 中国区 */
        private String baseUrl = "https://dashscope.aliyuncs.com";
        private int pollIntervalMs = 3000;
        private int maxPollAttempts = 40;
    }
}
