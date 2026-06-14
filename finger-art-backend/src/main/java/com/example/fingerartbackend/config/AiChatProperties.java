package com.example.fingerartbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.chat")
public class AiChatProperties {

    /** 是否启用通义千问对话（关闭则使用数据增强的规则回复） */
    private boolean enabled = true;

    /** 复用 DashScope API Key，也可单独配置 */
    private String apiKey = "";

    private String baseUrl = "https://dashscope.aliyuncs.com";

    /** qwen-turbo 更快更省，qwen-plus 更聪明 */
    private String model = "qwen-turbo";

    private int maxTokens = 800;

    private double temperature = 0.7;
}
