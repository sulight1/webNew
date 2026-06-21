package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;
    private Long exchangeId;

    private Long fromUserId;
    private String fromUserName;
    private Long toUserId;
    private String toUserName;

    private Integer score;

    @Column(columnDefinition = "TEXT")
    private String content;

    /** 关联作品（买家订单评价时写入，便于作品详情页展示） */
    private Long productId;

    /** 评价配图 JSON 数组 */
    @Column(columnDefinition = "TEXT")
    private String imageUrls;

    private String fromUserAvatar;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
