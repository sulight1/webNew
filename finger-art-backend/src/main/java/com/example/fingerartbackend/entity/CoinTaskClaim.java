package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 造物币任务领取记录实体，对应数据库表 coin_task_claims。
 * 防止每日任务或事件任务重复发放奖励。
 */
@Entity
@Data
@Table(name = "coin_task_claims", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"userId", "taskCode", "claimDate"}),
        @UniqueConstraint(columnNames = {"userId", "taskCode", "referenceId"})
})
/**
 * CoinTaskClaim实体，对应数据库表及 JPA 映射。
 */
public class CoinTaskClaim {
    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 领取用户 ID */
    private Long userId;

    /** 任务编码 */
    private String taskCode;

    /** 领取日期；每日任务按日期，事件任务可为 null */
    private LocalDate claimDate;

    /** 关联业务 ID（订单/交换等），防重复领取 */
    private Long referenceId;

    /** 本次发放造物币数量 */
    private Integer coinsGranted;

    /** 领取时间 */
    private LocalDateTime createdAt;

    /**
     * JPA 持久化前回调，自动写入创建时间。
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
