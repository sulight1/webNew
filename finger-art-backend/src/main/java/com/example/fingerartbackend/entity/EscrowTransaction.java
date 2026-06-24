package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 订单托管流水实体，对应数据库表 escrow_transactions。
 * 记录定金、尾款、释放、冻结、退款等托管资金变动。
 */
@Entity
@Data
@Table(name = "escrow_transactions")
public class EscrowTransaction {
    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联订单 ID */
    private Long orderId;

    /** 关联用户 ID */
    private Long userId;

    /** 变动金额 */
    private Double amount;

    /** 流水类型：DEPOSIT / BALANCE / RELEASE / FREEZE / REFUND */
    private String type;

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
