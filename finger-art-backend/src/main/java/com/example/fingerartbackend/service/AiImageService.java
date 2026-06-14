package com.example.fingerartbackend.service;

public interface AiImageService {
    /**
     * 根据提示词生成图片，保存到本地 uploads，返回可访问 URL。
     */
    String generateAndSave(String prompt);
}
