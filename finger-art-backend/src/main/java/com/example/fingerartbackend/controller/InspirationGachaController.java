package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.auth.AuthContext;
import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.service.InspirationGachaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 灵感扭蛋控制器。
 * 提供 AI 灵感抽取、配图生成及免费次数查询，对应 AI 创意激励模块。
 */
@RestController
@RequestMapping("/ai/inspiration-gacha")
public class InspirationGachaController {

    @Autowired
    private InspirationGachaService inspirationGachaService;

    /**
     * 查询当前用户的扭蛋状态（免费次数、造物币余额等）。
     *
     * @param userId 可选用户 ID，须与当前登录用户一致
     * @return 扭蛋状态信息
     */
    @GetMapping("/status")
    public Result<Map<String, Object>> status(@RequestParam(required = false) Long userId) {
        try {
            return Result.success(inspirationGachaService.getStatus(resolveUserId(userId)));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 执行一次灵感扭蛋抽取。
     *
     * @param body 含 userId、useFree（是否使用免费次数）的请求体
     * @return 抽取到的灵感内容
     */
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

    /**
     * 根据灵感提示词生成配图。
     *
     * @param body 含 userId、imagePrompt 的请求体
     * @return 生成图片的 URL 及相关信息
     */
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

    /**
     * 将请求体中的 userId 字段解析为 Long。
     *
     * @param userIdObj 原始 userId 对象
     * @return 解析后的用户 ID，无法解析时返回 null
     */
    private Long parseUserId(Object userIdObj) {
        if (userIdObj instanceof Number num) {
            return num.longValue();
        }
        if (userIdObj != null) {
            return Long.parseLong(userIdObj.toString());
        }
        return null;
    }

    /**
     * 解析并校验操作用户 ID，确保只能操作当前登录账号。
     *
     * @param userId 请求中传入的用户 ID，可为 null
     * @return 当前登录用户 ID
     */
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
