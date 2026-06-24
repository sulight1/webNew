package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 定制需求竞标实体，对应数据库表 custom_request_bids。
 * 手艺人针对某条定制需求提交的接单意向，同一需求同一手艺人仅一条。
 */
@Entity
@Data
@Table(name = "custom_request_bids", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "request_id", "artisan_id" })
})
/**
 * 定制需求实体，对应数据库表及 JPA 映射。
 */
public class CustomRequestBid {

    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联定制需求 ID */
    @Column(name = "request_id", nullable = false)
    private Long requestId;

    /** 竞标手艺人用户 ID */
    @Column(name = "artisan_id", nullable = false)
    private Long artisanId;

    /** 竞标留言/方案说明 */
    @Column(columnDefinition = "TEXT")
    private String message;

    /** 竞标状态：PENDING / SELECTED / REJECTED */
    private String status = "PENDING";

    /** 提交时间 */
    private LocalDateTime createTime;

    /**
     * JPA 持久化前回调，自动写入创建时间。
     */
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
