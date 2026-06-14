package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "platform_notifications")
public class PlatformNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    /** MATCH_REQUEST | EXCHANGE_CONFIRM | EXCHANGE_NO_SHOW | SYSTEM */
    private String type;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String linkUrl;

    private Boolean isRead = false;

    private LocalDateTime createdAt = LocalDateTime.now();
}
