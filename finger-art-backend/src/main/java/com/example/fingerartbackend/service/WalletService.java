package com.example.fingerartbackend.service;

import com.example.fingerartbackend.dto.WalletOperationResult;
import com.example.fingerartbackend.entity.WalletTransaction;
import org.springframework.data.domain.Page;

/**
 * 钱包服务接口，定义业务能力（业务服务接口）。
 */
public interface WalletService {
    WalletOperationResult recharge(Long userId, Double amount, String channel);

    WalletOperationResult withdraw(Long userId, Double amount, String channel);

    WalletTransaction rechargePrepay(Long userId, Double amount, String channel);

    WalletOperationResult rechargeConfirm(Long userId, String outTradeNo);

    WalletTransaction withdrawPrepay(Long userId, Double amount, String channel);

    WalletOperationResult withdrawConfirm(Long userId, String outTradeNo);

    Page<WalletTransaction> getTransactions(Long userId, int page, int size);
}
