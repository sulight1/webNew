package com.example.fingerartbackend.dto;

import com.example.fingerartbackend.entity.User;
import com.example.fingerartbackend.entity.WalletTransaction;
import lombok.Data;

@Data
public class WalletOperationResult {
    private User user;
    private WalletTransaction transaction;

    public WalletOperationResult(User user, WalletTransaction transaction) {
        this.user = user;
        this.transaction = transaction;
    }
}
