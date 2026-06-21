package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "review_replies")
public class ReviewReply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reviewId;
    private Long fromUserId;
    private String fromUserName;

    @Column(columnDefinition = "TEXT")
    private String content;

    /** 追评配图 JSON 数组 */
    @Column(columnDefinition = "TEXT")
    private String imageUrls;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
