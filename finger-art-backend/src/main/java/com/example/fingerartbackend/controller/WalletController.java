package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.dto.WalletOperationResult;
import com.example.fingerartbackend.entity.WalletTransaction;
import com.example.fingerartbackend.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 钱包控制器。
 * 处理造物币充值、提现及交易流水查询，对应用户钱包与支付模块。
 */
@RestController
@RequestMapping("/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    /**
     * 直接充值造物币（同步完成）。
     *
     * @param payload 含 userId、amount、channel 的请求体
     * @return 充值操作结果及余额变动
     */
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

    /**
     * 创建充值预支付订单。
     *
     * @param payload 含 userId、amount、channel 的请求体
     * @return 预支付交易记录（含 outTradeNo）
     */
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

    /**
     * 确认充值支付完成，入账造物币。
     *
     * @param payload 含 userId、outTradeNo 的请求体
     * @return 充值操作结果
     */
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

    /**
     * 直接提现造物币（同步完成）。
     *
     * @param payload 含 userId、amount、channel 的请求体
     * @return 提现操作结果及余额变动
     */
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

    /**
     * 创建提现预支付订单。
     *
     * @param payload 含 userId、amount、channel 的请求体
     * @return 预支付交易记录
     */
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

    /**
     * 确认提现处理完成。
     *
     * @param payload 含 userId、outTradeNo 的请求体
     * @return 提现操作结果
     */
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

    /**
     * 分页查询用户钱包交易流水。
     *
     * @param userId 用户 ID
     * @param page   页码，从 0 开始
     * @param size   每页条数
     * @return 交易流水分页数据
     */
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
