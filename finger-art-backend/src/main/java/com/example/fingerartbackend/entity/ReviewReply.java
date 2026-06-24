package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价回复实体，对应数据库表 review_replies。
 * 被评价人对原评价的追评或回复。
 */
@Entity
@Data
@Table(name = "review_replies")
public class ReviewReply {
    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联原评价 ID */
    private Long reviewId;

    /** 回复人用户 ID */
    private Long fromUserId;

    /** 回复人昵称 */
    private String fromUserName;

    /** 回复文字内容 */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** 追评配图 JSON 数组 */
    @Column(columnDefinition = "TEXT")
    private String imageUrls;

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
