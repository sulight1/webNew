package com.example.fingerartbackend.service.impl;

import com.example.fingerartbackend.dto.WalletOperationResult;
import com.example.fingerartbackend.entity.User;
import com.example.fingerartbackend.entity.WalletTransaction;
import com.example.fingerartbackend.mapper.WalletTransactionMapper;
import com.example.fingerartbackend.service.UserService;
import com.example.fingerartbackend.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 钱包服务实现类。
 */
@Service
public class WalletServiceImpl implements WalletService {

    private static final double MAX_RECHARGE = 5000.0;
    private static final double MIN_AMOUNT = 1.0;

    @Autowired
    private UserService userService;

    @Autowired
    private WalletTransactionMapper walletTransactionMapper;

    /**
     * 执行 recharge 相关逻辑。
     */
    @Override
    @Transactional
    public WalletOperationResult recharge(Long userId, Double amount, String channel) {
        WalletTransaction pending = rechargePrepay(userId, amount, channel);
        return rechargeConfirm(userId, pending.getOutTradeNo());
    }

    /**
     * 执行 withdraw 相关逻辑。
     */
    @Override
    @Transactional
    public WalletOperationResult withdraw(Long userId, Double amount, String channel) {
        WalletTransaction pending = withdrawPrepay(userId, amount, channel);
        return withdrawConfirm(userId, pending.getOutTradeNo());
    }

    /**
     * 执行 rechargePrepay 相关逻辑。
     */
    @Override
    @Transactional
    public WalletTransaction rechargePrepay(Long userId, Double amount, String channel) {
        validateAmount(amount);
        if (amount > MAX_RECHARGE) {
            throw new RuntimeException("单次充值不能超过 " + (int) MAX_RECHARGE + " 造物币");
        }
        User user = userService.getUserById(userId);
        double balance = user.getZaowuBiBalance() != null ? user.getZaowuBiBalance() : 0.0;
        return savePendingTransaction(
                userId,
                "RECHARGE",
                amount,
                balance,
                channel,
                rechargeChannelLabel(channel) + "充值 " + formatAmount(amount) + " 元",
                generateOutTradeNo("RCH")
        );
    }

    /**
     * 执行 rechargeConfirm 相关逻辑。
     */
    @Override
    @Transactional
    public WalletOperationResult rechargeConfirm(Long userId, String outTradeNo) {
        WalletTransaction tx = getPendingTransaction(userId, outTradeNo, "RECHARGE");
        double amount = Math.abs(tx.getAmount());
        User user = userService.addZaoWuBi(userId, amount);
        tx.setStatus("SUCCESS");
        tx.setBalanceAfter(round(user.getZaowuBiBalance()));
        tx.setRemark(rechargeChannelLabel(tx.getChannel()) + "充值成功 " + formatAmount(amount) + " 元");
        walletTransactionMapper.save(tx);
        return new WalletOperationResult(user, tx);
    }

    /**
     * 执行 withdrawPrepay 相关逻辑。
     */
    @Override
    @Transactional
    public WalletTransaction withdrawPrepay(Long userId, Double amount, String channel) {
        validateAmount(amount);
        User user = userService.getUserById(userId);
        double balance = user.getZaowuBiBalance() != null ? user.getZaowuBiBalance() : 0.0;
        if (balance < amount) {
            throw new RuntimeException("造物币余额不足，无法提现");
        }
        String channelLabel = "MOCK_BANK".equals(channel) ? "银行卡" : "微信零钱";
        return savePendingTransaction(
                userId,
                "WITHDRAW",
                -amount,
                balance,
                channel,
                "提现至" + channelLabel + " " + formatAmount(amount) + " 造物币",
                generateOutTradeNo("WD")
        );
    }

    /**
     * 执行 withdrawConfirm 相关逻辑。
     */
    @Override
    @Transactional
    public WalletOperationResult withdrawConfirm(Long userId, String outTradeNo) {
        WalletTransaction tx = getPendingTransaction(userId, outTradeNo, "WITHDRAW");
        double amount = Math.abs(tx.getAmount());
        User user = userService.addZaoWuBi(userId, -amount);
        tx.setStatus("SUCCESS");
        tx.setBalanceAfter(round(user.getZaowuBiBalance()));
        String channelLabel = "MOCK_BANK".equals(tx.getChannel()) ? "银行卡" : "微信零钱";
        tx.setRemark("提现至" + channelLabel + "成功 " + formatAmount(amount) + " 造物币");
        walletTransactionMapper.save(tx);
        return new WalletOperationResult(user, tx);
    }

    /**
     * 查询钱包信息。
     */
    @Override
    public Page<WalletTransaction> getTransactions(Long userId, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        return walletTransactionMapper.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(safePage, safeSize));
    }

    /**
     * 查询钱包信息。
     */
    private WalletTransaction getPendingTransaction(Long userId, String outTradeNo, String type) {
        if (outTradeNo == null || outTradeNo.isBlank()) {
            throw new RuntimeException("订单号不能为空");
        }
        WalletTransaction tx = walletTransactionMapper.findByOutTradeNoAndUserId(outTradeNo, userId)
                .orElseThrow(() -> new RuntimeException("支付订单不存在或已失效"));
        if (!type.equals(tx.getType())) {
            throw new RuntimeException("订单类型不匹配");
        }
        if (!"PENDING".equals(tx.getStatus())) {
            throw new RuntimeException("订单已处理，请勿重复操作");
        }
        return tx;
    }

    /**
     * 保存钱包。
     */
    private WalletTransaction savePendingTransaction(
            Long userId,
            String type,
            Double amount,
            Double currentBalance,
            String channel,
            String remark,
            String outTradeNo
    ) {
        WalletTransaction tx = new WalletTransaction();
        tx.setUserId(userId);
        tx.setType(type);
        tx.setAmount(round(amount));
        tx.setBalanceAfter(round(currentBalance));
        tx.setStatus("PENDING");
        tx.setChannel(channel != null && !channel.isEmpty() ? channel : "MOCK_WECHAT");
        tx.setRemark(remark);
        tx.setOutTradeNo(outTradeNo);
        return walletTransactionMapper.save(tx);
    }

    /**
     * 校验数据。
     */
    private void validateAmount(Double amount) {
        if (amount == null || amount < MIN_AMOUNT) {
            throw new RuntimeException("金额不能小于 " + (int) MIN_AMOUNT + " 造物币");
        }
    }

    /**
     * 生成令牌或数据。
     */
    private String generateOutTradeNo(String prefix) {
        int suffix = ThreadLocalRandom.current().nextInt(1000, 10000);
        return prefix + System.currentTimeMillis() + suffix;
    }

    /**
     * 执行 round 相关逻辑。
     */
    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * 执行 formatAmount 相关逻辑。
     */
    private String formatAmount(double amount) {
        return String.format("%.2f", amount);
    }

    /**
     * 执行 rechargeChannelLabel 相关逻辑。
     */
    private String rechargeChannelLabel(String channel) {
        if ("MOCK_ALIPAY".equals(channel)) {
            return "支付宝";
        }
        return "微信支付";
    }
}
