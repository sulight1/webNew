package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 内容举报实体，对应数据库表 content_reports。
 * 用户对作品、技能、帖子等违规内容的举报及管理员处理记录。
 */
@Entity
@Data
@Table(name = "content_reports")
public class ContentReport {
    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 举报人用户 ID */
    private Long reporterId;

    /** 举报人昵称 */
    private String reporterName;

    /** 举报目标类型：PRODUCT / SKILL / USER / ORDER / MESSAGE / FORUM_POST / FORUM_REPLY */
    private String targetType;

    /** 举报目标 ID */
    private Long targetId;

    /** 举报目标标题（冗余展示） */
    private String targetTitle;

    /** 举报原因分类 */
    private String reason;

    /** 举报详细说明 */
    @Column(columnDefinition = "TEXT")
    private String detail;

    /** 处理状态：PENDING / HANDLED / REJECTED */
    private String status = "PENDING";

    /** 管理员处理备注 */
    @Column(columnDefinition = "TEXT")
    private String handleNote;

    /** 处理管理员 ID */
    private Long handlerId;

    /** 举报提交时间 */
    private LocalDateTime createdAt;

    /** 处理完成时间 */
    private LocalDateTime handledAt;

    /**
     * JPA 持久化前回调，自动写入创建时间。
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
