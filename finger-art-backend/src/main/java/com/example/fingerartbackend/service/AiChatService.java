package com.example.fingerartbackend.service;

import java.util.List;
import java.util.Map;

/**
 * AI 对话服务接口，定义业务能力（业务服务接口）。
 */
public interface AiChatService {

    Map<String, Object> chat(List<Map<String, String>> messages, Long userId, String pageContext);

    Map<String, Object> recommend(String query, int limit);

    /** 推断作品分类，返回 category（key）、source（qwen/rules/fallback） */
    Map<String, Object> classifyProductCategory(String keywords, String imageUrl);
}
