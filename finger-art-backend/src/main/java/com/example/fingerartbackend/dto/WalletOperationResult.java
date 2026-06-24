package com.example.fingerartbackend.dto;

import com.example.fingerartbackend.entity.User;
import com.example.fingerartbackend.entity.WalletTransaction;
import lombok.Data;

/**
 * 钱包操作结果 DTO。
 * 充值/提现等操作完成后返回最新用户余额及流水记录。
 */
@Data
public class WalletOperationResult {
    /** 操作后的用户信息（含最新余额） */
    private User user;

    /** 本次钱包流水记录 */
    private WalletTransaction transaction;

    public WalletOperationResult(User user, WalletTransaction transaction) {
        this.user = user;
        this.transaction = transaction;
    }
}
