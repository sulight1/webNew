package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 平台通知实体，对应数据库表 platform_notifications。
 * 向用户推送匹配、交换、系统等站内消息。
 */
@Entity
@Data
@Table(name = "platform_notifications")
public class PlatformNotification {
    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 接收用户 ID */
    private Long userId;

    /** 通知类型：MATCH_REQUEST / EXCHANGE_CONFIRM / EXCHANGE_NO_SHOW / SYSTEM */
    private String type;

    /** 通知标题 */
    private String title;

    /** 通知正文 */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** 点击跳转链接 */
    private String linkUrl;

    /** 是否已读 */
    private Boolean isRead = false;

    /** 创建时间 */
    private LocalDateTime createdAt = LocalDateTime.now();
}
