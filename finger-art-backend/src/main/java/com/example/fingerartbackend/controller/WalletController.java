package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.dto.WalletOperationResult;
import com.example.fingerartbackend.entity.WalletTransaction;
import com.example.fingerartbackend.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @PostMapping("/recharge")
    public Result<WalletOperationResult> recharge(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.valueOf(payload.get("userId").toString());
            Double amount = Double.valueOf(payload.get("amount").toString());
            String channel = payload.get("channel") != null ? payload.get("channel").toString() : "MOCK_WECHAT";
            return Result.success(walletService.recharge(userId, amount, channel));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/recharge/prepay")
    public Result<WalletTransaction> rechargePrepay(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.valueOf(payload.get("userId").toString());
            Double amount = Double.valueOf(payload.get("amount").toString());
            String channel = payload.get("channel") != null ? payload.get("channel").toString() : "MOCK_WECHAT";
            return Result.success(walletService.rechargePrepay(userId, amount, channel));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/recharge/confirm")
    public Result<WalletOperationResult> rechargeConfirm(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.valueOf(payload.get("userId").toString());
            String outTradeNo = payload.get("outTradeNo").toString();
            return Result.success(walletService.rechargeConfirm(userId, outTradeNo));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/withdraw")
    public Result<WalletOperationResult> withdraw(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.valueOf(payload.get("userId").toString());
            Double amount = Double.valueOf(payload.get("amount").toString());
            String channel = payload.get("channel") != null ? payload.get("channel").toString() : "MOCK_WECHAT";
            return Result.success(walletService.withdraw(userId, amount, channel));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/withdraw/prepay")
    public Result<WalletTransaction> withdrawPrepay(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.valueOf(payload.get("userId").toString());
            Double amount = Double.valueOf(payload.get("amount").toString());
            String channel = payload.get("channel") != null ? payload.get("channel").toString() : "MOCK_WECHAT";
            return Result.success(walletService.withdrawPrepay(userId, amount, channel));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/withdraw/confirm")
    public Result<WalletOperationResult> withdrawConfirm(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.valueOf(payload.get("userId").toString());
            String outTradeNo = payload.get("outTradeNo").toString();
            return Result.success(walletService.withdrawConfirm(userId, outTradeNo));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/transactions")
    public Result<Page<WalletTransaction>> getTransactions(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            return Result.success(walletService.getTransactions(userId, page, size));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
