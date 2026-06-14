package com.example.fingerartbackend.service;

import com.example.fingerartbackend.entity.Product;

import java.util.List;
import java.util.Map;

public interface CoinEconomyService {
    Map<String, Object> checkIn(Long userId);
    List<Map<String, Object>> getTaskStatus(Long userId);
    Map<String, Object> claimDailyTask(Long userId, String taskCode);
    void grantEventReward(Long userId, String taskCode, Long referenceId, int coins, String title);
    Product boostProductExposure(Long userId, Long productId);
}
