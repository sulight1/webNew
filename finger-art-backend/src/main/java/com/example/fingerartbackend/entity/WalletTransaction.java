package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 钱包流水实体，对应数据库表 wallet_transactions。
 * 记录造物币充值、提现等账户变动。
 */
@Entity
@Data
@Table(name = "wallet_transactions")
public class WalletTransaction {
    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 流水类型：RECHARGE（充值）/ WITHDRAW（提现） */
    private String type;

    /** 变动金额 */
    private Double amount;

    /** 变动后余额 */
    private Double balanceAfter;

    /** 处理状态：PENDING / SUCCESS / FAILED */
    private String status;

    /** 商户订单号，prepay 时生成 */
    @Column(unique = true)
    private String outTradeNo;

    /** 支付渠道：MOCK_WECHAT / MOCK_BANK */
    private String channel;

    /** 备注说明 */
    @Column(columnDefinition = "TEXT")
    private String remark;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /**
     * JPA 持久化前回调，自动写入创建时间。
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
