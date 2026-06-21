package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "user_punishments")
public class UserPunishment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    /** ACCOUNT_BAN / NO_ORDER / NO_FORUM / NO_PRODUCT / NO_SKILL */
    private String type;

    private LocalDateTime startAt;

    /** null 表示永久处罚 */
    private LocalDateTime endAt;

    @Column(columnDefinition = "TEXT")
    private String reason;

    private Long adminId;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        if (startAt == null) {
            startAt = now;
        }
    }
}
