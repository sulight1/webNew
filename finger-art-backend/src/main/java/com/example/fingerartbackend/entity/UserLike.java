package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户点赞记录实体，对应数据库表 user_likes。
 * 记录用户对作品或帖子的点赞，同一目标仅一条。
 */
@Entity
@Data
@Table(name = "user_likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "target_type", "target_id"})
})
/**
 * 用户实体，对应数据库表及 JPA 映射。
 */
public class UserLike {
    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 点赞用户 ID */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 点赞目标类型：PRODUCT / FORUM_POST */
    @Column(name = "target_type", nullable = false)
    private String targetType;

    /** 点赞目标 ID */
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    /** 点赞时间 */
    private LocalDateTime createTime;

    /**
     * JPA 持久化前回调，自动写入创建时间。
     */
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
