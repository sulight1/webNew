package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 订单里程碑实体，对应数据库表 order_milestones。
 * 记录定制/成品订单各阶段进度节点及操作凭证。
 */
@Entity
@Data
@Table(name = "order_milestones")
public class OrderMilestone {
    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联订单 ID */
    private Long orderId;

    /** 阶段标识：CONFIRM / DEPOSIT / PRODUCING / SHIP / ACCEPT / BALANCE / COMPLETE */
    private String stageKey;

    /** 阶段展示名称 */
    private String stageLabel;

    /** 阶段备注说明 */
    @Column(columnDefinition = "TEXT")
    private String note;

    /** 阶段凭证图片 URL */
    private String imageUrl;

    /** 操作人用户 ID */
    private Long operatorId;

    /** 操作人昵称 */
    private String operatorName;

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
