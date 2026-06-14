package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.mapper.CustomOrderMapper;
import com.example.fingerartbackend.mapper.CustomRequestMapper;
import com.example.fingerartbackend.mapper.UserMapper;
import com.example.fingerartbackend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/stats")
public class SystemStatsController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CustomOrderMapper orderMapper;

    @Autowired
    private CustomRequestMapper requestMapper;

    @Autowired
    private com.example.fingerartbackend.mapper.SkillMapper skillMapper;

    @Autowired
    private com.example.fingerartbackend.mapper.SkillExchangeRepository skillExchangeRepository;

    @GetMapping("/summary")
    public Result<Map<String, Object>> getSystemSummary() {
        Map<String, Object> stats = new HashMap<>();
        
        long activeRequests = requestMapper.countByStatus("OPEN");
        long artisanCount = userMapper.countByRole("ARTISAN");
        
        long totalOrders = orderMapper.count();
        long completedOrders = orderMapper.countByStatus("COMPLETED");
        
        double completionRate = 0.0;
        if (totalOrders > 0) {
            completionRate = (double) completedOrders / totalOrders * 100;
        } else {
            completionRate = 0.0;
        }

        // 技能交换统计
        long skillCount = skillMapper.count();
        long exchangeCount = skillExchangeRepository.count();
        
        stats.put("activeRequests", activeRequests);
        stats.put("artisanCount", artisanCount);
        stats.put("completionRate", String.format("%.1f%%", completionRate));
        
        // 技能交换相关
        stats.put("skillCount", skillCount);
        stats.put("totalExchanges", exchangeCount);
        stats.put("avgRating", userMapper.findAll().stream()
                .filter(u -> u.getRating() != null && u.getReviewCount() != null && u.getReviewCount() > 0)
                .mapToDouble(User::getRating)
                .average()
                .orElse(5.0));
        
        return Result.success(stats);
    }
}
