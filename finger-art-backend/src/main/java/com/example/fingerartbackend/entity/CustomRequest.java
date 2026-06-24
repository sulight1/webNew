package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 定制需求实体，对应数据库表 custom_requests。
 * 买家发布定制意向，供手艺人竞标或系统匹配。
 */
@Entity
@Data
@Table(name = "custom_requests")
public class CustomRequest {
    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 发布需求的买家 */
    @ManyToOne
    @JoinColumn(name = "buyer_id")
    private User buyer;

    /** 需求标题 */
    private String title;

    /** 需求分类 */
    private String category;

    /** 需求详细描述 */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 预算下限（元） */
    private Double budgetMin;

    /** 预算上限（元） */
    private Double budgetMax;

    /** 期望交付期限 */
    private String deadline;

    /** 参考图 / AI 灵感图 URL */
    private String referenceImage;

    /** 状态：PENDING（待审核）、OPEN（招募中）、MATCHED（已匹配）、CLOSED（已关闭）、COMPLETED（已完成）、REJECTED（已拒绝） */
    private String status = "PENDING";

    /** 创建时间 */
    private LocalDateTime createTime;

    /**
     * JPA 持久化前回调，自动写入创建时间。
     */
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
