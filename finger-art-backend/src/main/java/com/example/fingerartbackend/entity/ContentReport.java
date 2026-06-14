package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "content_reports")
public class ContentReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reporterId;
    private String reporterName;

    /** PRODUCT / SKILL / USER / ORDER / MESSAGE / FORUM_POST / FORUM_REPLY */
    private String targetType;
    private Long targetId;
    private String targetTitle;

    private String reason;

    @Column(columnDefinition = "TEXT")
    private String detail;

    /** PENDING / HANDLED / REJECTED */
    private String status = "PENDING";

    @Column(columnDefinition = "TEXT")
    private String handleNote;

    private Long handlerId;
    private LocalDateTime createdAt;
    private LocalDateTime handledAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
