package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "coin_task_claims", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"userId", "taskCode", "claimDate"}),
        @UniqueConstraint(columnNames = {"userId", "taskCode", "referenceId"})
})
public class CoinTaskClaim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String taskCode;

    /** 每日任务按日期；事件任务可为 null */
    private LocalDate claimDate;

    /** 订单/交换等业务 ID，防重复领取 */
    private Long referenceId;

    private Integer coinsGranted;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
