package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "wallet_transactions")
public class WalletTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    /** RECHARGE / WITHDRAW */
    private String type;

    private Double amount;

    private Double balanceAfter;

    /** PENDING / SUCCESS / FAILED */
    private String status;

    /** 商户订单号，prepay 时生成 */
    @Column(unique = true)
    private String outTradeNo;

    /** MOCK_WECHAT / MOCK_BANK */
    private String channel;

    @Column(columnDefinition = "TEXT")
    private String remark;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
