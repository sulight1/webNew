package com.example.fingerartbackend.service;

import java.util.Map;

/**
 * 灵感扭蛋服务接口，定义业务能力（业务服务接口）。
 */
public interface InspirationGachaService {

    Map<String, Object> getStatus(Long userId);

    Map<String, Object> draw(Long userId, boolean useFree);

    Map<String, Object> generateImage(Long userId, String imagePrompt);
}
