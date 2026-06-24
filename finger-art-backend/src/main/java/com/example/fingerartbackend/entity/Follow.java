package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户关注关系实体，对应数据库表 follows。
 * 记录 follower 关注 following 的单向关系。
 */
@Entity
@Data
@Table(name = "follows", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"follower_id", "following_id"})
})
/**
 * Follow实体，对应数据库表及 JPA 映射。
 */
public class Follow {
    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关注者用户 ID */
    @Column(name = "follower_id", nullable = false)
    private Long followerId;

    /** 被关注者用户 ID */
    @Column(name = "following_id", nullable = false)
    private Long followingId;

    /** 关注时间 */
    private LocalDateTime createTime;

    /**
     * JPA 持久化前回调，自动写入创建时间。
     */
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
