package com.example.fingerartbackend.controller;

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
    public Result<Map<String, Object>> checkIn(@RequestParam Long userId) {
        return Result.success(coinEconomyService.checkIn(userId));
    }

    @GetMapping("/tasks")
    public Result<List<Map<String, Object>>> tasks(@RequestParam Long userId) {
        return Result.success(coinEconomyService.getTaskStatus(userId));
    }

    @PostMapping("/tasks/claim")
    public Result<Map<String, Object>> claimTask(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        String taskCode = body.get("taskCode").toString();
        return Result.success(coinEconomyService.claimDailyTask(userId, taskCode));
    }

    @PostMapping("/boost-product")
    public Result<Product> boostProduct(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        Long productId = Long.valueOf(body.get("productId").toString());
        return Result.success(coinEconomyService.boostProductExposure(userId, productId));
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
