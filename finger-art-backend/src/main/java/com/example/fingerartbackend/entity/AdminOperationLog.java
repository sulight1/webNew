package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员操作日志实体，对应数据库表 admin_operation_logs。
 * 审计管理员在后台的关键操作行为。
 */
@Entity
@Data
@Table(name = "admin_operation_logs")
public class AdminOperationLog {

    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 操作管理员 ID */
    private Long adminId;

    /** 操作管理员用户名 */
    private String adminUsername;

    /** 操作动作：DELETE_USER / AUDIT_PRODUCT / BATCH_AUDIT_PRODUCT 等 */
    private String action;

    /** 操作目标类型：USER / PRODUCT / SKILL / ORDER / FORUM / REPORT */
    private String targetType;

    /** 操作目标 ID */
    private Long targetId;

    /** 操作详情（JSON 或文本） */
    @Column(columnDefinition = "TEXT")
    private String detail;

    /** 操作来源 IP */
    private String ip;

    /** 操作时间 */
    private LocalDateTime createdAt;

    /**
     * JPA 持久化前回调，自动写入创建时间。
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
