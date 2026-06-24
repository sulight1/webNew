package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评价实体，对应数据库表 reviews。
 * 用于订单或技能交换完成后的双向评分与图文评价。
 */
@Entity
@Data
@Table(name = "reviews")
public class Review {
    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联订单 ID（订单评价时填写） */
    private Long orderId;

    /** 关联技能交换 ID（交换评价时填写） */
    private Long exchangeId;

    /** 评价人用户 ID */
    private Long fromUserId;

    /** 评价人昵称 */
    private String fromUserName;

    /** 被评价人用户 ID */
    private Long toUserId;

    /** 被评价人昵称 */
    private String toUserName;

    /** 评分（1-5） */
    private Integer score;

    /** 评价文字内容 */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** 关联作品（买家订单评价时写入，便于作品详情页展示） */
    private Long productId;

    /** 评价配图 JSON 数组 */
    @Column(columnDefinition = "TEXT")
    private String imageUrls;

    /** 评价人头像 URL */
    private String fromUserAvatar;

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
