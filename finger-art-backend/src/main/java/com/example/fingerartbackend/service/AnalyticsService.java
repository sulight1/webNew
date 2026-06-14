package com.example.fingerartbackend.service;

import java.util.Map;

public interface AnalyticsService {
    Map<String, Object> getPlatformAnalytics();
    Map<String, Object> getArtisanAnalytics(Long userId);
}
