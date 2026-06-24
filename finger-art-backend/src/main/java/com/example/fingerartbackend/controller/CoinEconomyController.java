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

/**
 * 造物币经济控制器。
 * 负责签到、每日任务、作品曝光加速及敏感词管理，对应平台激励与内容风控模块。
 */
@RestController
@RequestMapping("/economy")
public class CoinEconomyController {

    @Autowired
    private CoinEconomyService coinEconomyService;
    @Autowired
    private SensitiveWordService sensitiveWordService;

    /**
     * 用户每日签到，领取造物币奖励。
     *
     * @param userId 可选用户 ID，须与当前登录用户一致
     * @return 签到结果及奖励信息
     */
    @PostMapping("/check-in")
    public Result<Map<String, Object>> checkIn(@RequestParam(required = false) Long userId) {
        try {
            return Result.success(coinEconomyService.checkIn(resolveUserId(userId)));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询当前用户的每日任务完成状态。
     *
     * @param userId 可选用户 ID，须与当前登录用户一致
     * @return 任务列表及完成/领取状态
     */
    @GetMapping("/tasks")
    public Result<List<Map<String, Object>>> tasks(@RequestParam(required = false) Long userId) {
        try {
            return Result.success(coinEconomyService.getTaskStatus(resolveUserId(userId)));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 领取指定每日任务的造物币奖励。
     *
     * @param body 含 userId、taskCode 的请求体
     * @return 领取结果及奖励信息
     */
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

    /**
     * 消耗造物币为作品增加曝光权重。
     *
     * @param body 含 userId、productId 的请求体
     * @return 加速后的作品信息
     */
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

    /**
     * 查询敏感词列表及详情。
     *
     * @return 敏感词详情列表
     */
    @GetMapping("/sensitive-words")
    public Result<List<Map<String, Object>>> sensitiveWords() {
        return Result.success(sensitiveWordService.listWordDetails());
    }

    /**
     * 新增敏感词。
     *
     * @param body 含 word 字段的请求体
     * @return 添加成功提示
     */
    @PostMapping("/sensitive-words")
    public Result<String> addSensitiveWord(@RequestBody Map<String, String> body) {
        sensitiveWordService.addWord(body.get("word"));
        return Result.success("已添加");
    }

    /**
     * 删除指定敏感词。
     *
     * @param id 敏感词记录 ID
     * @return 删除成功提示
     */
    @DeleteMapping("/sensitive-words/{id}")
    public Result<String> removeSensitiveWord(@PathVariable Long id) {
        sensitiveWordService.removeWord(id);
        return Result.success("已删除");
    }
}
