package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.entity.SkillExchange;
import com.example.fingerartbackend.service.SkillExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * 技能交换控制器。
 * 管理技能交换的发起、接受、确认、完成及爽约上报，对应技能交换交易模块。
 */
@RestController
@RequestMapping("/skill-exchange")
public class SkillExchangeController {

    @Autowired
    private SkillExchangeService exchangeService;

    /**
     * 发起技能交换请求。
     *
     * @param payload 含 userAId、userBId、description、zaowuBiCost、scheduleDate 的请求体
     * @return 创建的交换记录
     */
    @PostMapping("/request")
    public Result<SkillExchange> requestExchange(@RequestBody Map<String, Object> payload) {
        try {
            Long userAId = Long.valueOf(payload.get("userAId").toString());
            Long userBId = Long.valueOf(payload.get("userBId").toString());
            String description = payload.get("description").toString();
            Integer cost = Integer.valueOf(payload.get("zaowuBiCost").toString());
            String scheduleDate = payload.get("scheduleDate") != null ? payload.get("scheduleDate").toString() : null;
            return Result.success(exchangeService.requestExchange(userAId, userBId, description, cost, scheduleDate));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 接受技能交换请求。
     *
     * @param id   交换记录 ID
     * @param body 含 userId 的请求体
     * @return 更新后的交换记录
     */
    @PatchMapping("/{id}/accept")
    public Result<SkillExchange> acceptExchange(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        try {
            return Result.success(exchangeService.acceptExchange(id, body.get("userId")));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 确认技能交换（双方确认见面/开始）。
     *
     * @param id   交换记录 ID
     * @param body 含 userId 的请求体
     * @return 更新后的交换记录
     */
    @PatchMapping("/{id}/confirm")
    public Result<SkillExchange> confirmExchange(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        try {
            return Result.success(exchangeService.confirmExchange(id, body.get("userId")));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 标记技能交换已完成。
     *
     * @param id   交换记录 ID
     * @param body 含 userId 的请求体
     * @return 更新后的交换记录
     */
    @PatchMapping("/{id}/complete")
    public Result<SkillExchange> completeExchange(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        try {
            Long userId = body != null && body.get("userId") != null ? body.get("userId") : null;
            if (userId == null) {
                return Result.error("缺少 userId");
            }
            return Result.success(exchangeService.completeExchange(id, userId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 上报对方爽约（未到场）。
     *
     * @param id   交换记录 ID
     * @param body 含 userId 的请求体
     * @return 更新后的交换记录
     */
    @PatchMapping("/{id}/no-show")
    public Result<SkillExchange> reportNoShow(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        try {
            return Result.success(exchangeService.reportNoShow(id, body.get("userId")));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询用户参与的全部技能交换记录。
     *
     * @param userId 用户 ID
     * @return 交换记录列表
     */
    @GetMapping("/my")
    public Result<List<SkillExchange>> getMyExchanges(@RequestParam Long userId) {
        try {
            return Result.success(exchangeService.getMyExchanges(userId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
