package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "admin_operation_logs")
public class AdminOperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long adminId;

    private String adminUsername;

    /** DELETE_USER / AUDIT_PRODUCT / BATCH_AUDIT_PRODUCT / ... */
    private String action;

    /** USER / PRODUCT / SKILL / ORDER / FORUM / REPORT */
    private String targetType;

    private Long targetId;

    @Column(columnDefinition = "TEXT")
    private String detail;

    private String ip;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
