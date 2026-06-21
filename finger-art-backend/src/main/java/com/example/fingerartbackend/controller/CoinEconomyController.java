package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.auth.AuthContext;
import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.entity.Product;
import com.example.fingerartbackend.service.CoinEconomyService;
import com.example.fingerartbackend.service.SensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/economy")
public class CoinEconomyController {

    @Autowired
    private CoinEconomyService coinEconomyService;
    @Autowired
    private SensitiveWordService sensitiveWordService;

    @PostMapping("/check-in")
    public Result<Map<String, Object>> checkIn(@RequestParam(required = false) Long userId) {
        try {
            return Result.success(coinEconomyService.checkIn(resolveUserId(userId)));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/tasks")
    public Result<List<Map<String, Object>>> tasks(@RequestParam(required = false) Long userId) {
        try {
            return Result.success(coinEconomyService.getTaskStatus(resolveUserId(userId)));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/tasks/claim")
    public Result<Map<String, Object>> claimTask(@RequestBody Map<String, Object> body) {
        try {
            Long userId = body.get("userId") != null ? Long.valueOf(body.get("userId").toString()) : null;
            String taskCode = body.get("taskCode").toString();
            return Result.success(coinEconomyService.claimDailyTask(resolveUserId(userId), taskCode));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/boost-product")
    public Result<Product> boostProduct(@RequestBody Map<String, Object> body) {
        try {
            Long userId = body.get("userId") != null ? Long.valueOf(body.get("userId").toString()) : null;
            Long productId = Long.valueOf(body.get("productId").toString());
            return Result.success(coinEconomyService.boostProductExposure(resolveUserId(userId), productId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    private Long resolveUserId(Long userId) {
        Long authUserId = AuthContext.getUserId();
        if (authUserId == null) {
            throw new RuntimeException("请先登录");
        }
        if (userId != null && !userId.equals(authUserId)) {
            throw new RuntimeException("无权操作其他账号");
        }
        return authUserId;
    }

    @GetMapping("/sensitive-words")
    public Result<List<Map<String, Object>>> sensitiveWords() {
        return Result.success(sensitiveWordService.listWordDetails());
    }

    @PostMapping("/sensitive-words")
    public Result<String> addSensitiveWord(@RequestBody Map<String, String> body) {
        sensitiveWordService.addWord(body.get("word"));
        return Result.success("已添加");
    }

    @DeleteMapping("/sensitive-words/{id}")
    public Result<String> removeSensitiveWord(@PathVariable Long id) {
        sensitiveWordService.removeWord(id);
        return Result.success("已删除");
    }
}
