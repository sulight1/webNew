package com.example.fingerartbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 对话服务配置属性，绑定 {@code ai.chat.*} 配置项。
 * <p>
 * 控制通义千问对话功能的开关、模型参数及 DashScope API 连接信息。
 * </p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai.chat")
public class AiChatProperties {

    /** 是否启用通义千问对话（关闭则使用数据增强的规则回复） */
    private boolean enabled = true;

    /** 复用 DashScope API Key，也可单独配置 */
    private String apiKey = "";

    /** DashScope API 基础地址 */
    private String baseUrl = "https://dashscope.aliyuncs.com";

    /** qwen-turbo 更快更省，qwen-plus 更聪明 */
    private String model = "qwen-turbo";

    /** 单次回复最大 Token 数 */
    private int maxTokens = 800;

    /** 采样温度，越高回复越随机 */
    private double temperature = 0.7;
}
