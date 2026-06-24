package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 数据分析控制器。
 * 提供平台级与手作达人维度的运营统计数据，对应数据分析模块。
 */
@RestController
@RequestMapping("/stats")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    /**
     * 获取平台整体运营分析数据。
     *
     * @return 平台统计指标集合
     */
    @GetMapping("/platform")
    public Result<Map<String, Object>> platform() {
        return Result.success(analyticsService.getPlatformAnalytics());
    }

    /**
     * 获取指定手作达人的经营分析数据。
     *
     * @param userId 手作达人用户 ID
     * @return 达人维度统计指标
     */
    @GetMapping("/artisan/{userId}")
    public Result<Map<String, Object>> artisan(@PathVariable Long userId) {
        return Result.success(analyticsService.getArtisanAnalytics(userId));
    }
}
