package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.entity.SkillExchange;
import com.example.fingerartbackend.service.SkillExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/skill-exchange")
public class SkillExchangeController {

    @Autowired
    private SkillExchangeService exchangeService;

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

    @PatchMapping("/{id}/accept")
    public Result<SkillExchange> acceptExchange(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        try {
            return Result.success(exchangeService.acceptExchange(id, body.get("userId")));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PatchMapping("/{id}/confirm")
    public Result<SkillExchange> confirmExchange(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        try {
            return Result.success(exchangeService.confirmExchange(id, body.get("userId")));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

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

    @PatchMapping("/{id}/no-show")
    public Result<SkillExchange> reportNoShow(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        try {
            return Result.success(exchangeService.reportNoShow(id, body.get("userId")));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/my")
    public Result<List<SkillExchange>> getMyExchanges(@RequestParam Long userId) {
        try {
            return Result.success(exchangeService.getMyExchanges(userId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
