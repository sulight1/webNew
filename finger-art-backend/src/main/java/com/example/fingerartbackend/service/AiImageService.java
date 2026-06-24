package com.example.fingerartbackend.service;

/**
 * AI 图像服务接口，定义业务能力（业务服务接口）。
 */
public interface AiImageService {
    /**
     * 根据提示词生成图片，保存到本地 uploads，返回可访问 URL。
     */
    String generateAndSave(String prompt);
}
