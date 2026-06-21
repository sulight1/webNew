package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.auth.AuthContext;
import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.service.InspirationGachaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/ai/inspiration-gacha")
public class InspirationGachaController {

    @Autowired
    private InspirationGachaService inspirationGachaService;

    @GetMapping("/status")
    public Result<Map<String, Object>> status(@RequestParam(required = false) Long userId) {
        try {
            return Result.success(inspirationGachaService.getStatus(resolveUserId(userId)));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/draw")
    public Result<Map<String, Object>> draw(@RequestBody Map<String, Object> body) {
        try {
            Long userId = resolveUserId(parseUserId(body.get("userId")));
            boolean useFree = body.get("useFree") == null || Boolean.parseBoolean(body.get("useFree").toString());
            return Result.success(inspirationGachaService.draw(userId, useFree));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/generate-image")
    public Result<Map<String, Object>> generateImage(@RequestBody Map<String, Object> body) {
        try {
            Long userId = resolveUserId(parseUserId(body.get("userId")));
            String imagePrompt = body.get("imagePrompt") != null ? body.get("imagePrompt").toString() : null;
            return Result.success(inspirationGachaService.generateImage(userId, imagePrompt));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    private Long parseUserId(Object userIdObj) {
        if (userIdObj instanceof Number num) {
            return num.longValue();
        }
        if (userIdObj != null) {
            return Long.parseLong(userIdObj.toString());
        }
        return null;
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
}
