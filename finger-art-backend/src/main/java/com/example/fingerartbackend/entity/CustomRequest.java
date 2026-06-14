package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "custom_requests")
public class CustomRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "buyer_id")
    private User buyer;

    private String title;
    private String category;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private Double budgetMin;
    private Double budgetMax;
    private String deadline;

    /** 参考图 / AI 灵感图 URL */
    private String referenceImage;
    
    // 状态：OPEN(招募中), CLOSED(已关闭), COMPLETED(已完成)
    private String status = "OPEN";

    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
