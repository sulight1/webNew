package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/stats")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/platform")
    public Result<Map<String, Object>> platform() {
        return Result.success(analyticsService.getPlatformAnalytics());
    }

    @GetMapping("/artisan/{userId}")
    public Result<Map<String, Object>> artisan(@PathVariable Long userId) {
        return Result.success(analyticsService.getArtisanAnalytics(userId));
    }
}
