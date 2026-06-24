package com.example.fingerartbackend.service;

import java.util.Map;

/**
 * 数据分析服务接口，定义业务能力（业务服务接口）。
 */
public interface AnalyticsService {
    Map<String, Object> getPlatformAnalytics();
    Map<String, Object> getArtisanAnalytics(Long userId);
}
