package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户处罚记录实体，对应数据库表 user_punishments。
 * 管理员对用户实施的封禁、禁言、禁下单等限制。
 */
@Entity
@Data
@Table(name = "user_punishments")
public class UserPunishment {

    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 被处罚用户 ID */
    private Long userId;

    /** 处罚类型：ACCOUNT_BAN / NO_ORDER / NO_FORUM / NO_PRODUCT / NO_SKILL */
    private String type;

    /** 处罚开始时间 */
    private LocalDateTime startAt;

    /** 处罚结束时间；null 表示永久处罚 */
    private LocalDateTime endAt;

    /** 处罚原因 */
    @Column(columnDefinition = "TEXT")
    private String reason;

    /** 执行处罚的管理员 ID */
    private Long adminId;

    /** 记录创建时间 */
    private LocalDateTime createdAt;

    /**
     * JPA 持久化前回调，自动写入创建时间。
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        if (startAt == null) {
            startAt = now;
        }
    }
}
