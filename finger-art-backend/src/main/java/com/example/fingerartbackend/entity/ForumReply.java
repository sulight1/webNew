package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 社区帖子回复实体，对应数据库表 forum_replies。
 * 记录用户对论坛帖子的评论内容。
 */
@Entity
@Data
@Table(name = "forum_replies")
public class ForumReply {
    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联帖子 ID */
    private Long postId;

    /** 回复人用户 ID */
    private Long authorId;

    /** 回复人昵称 */
    private String authorName;

    /** 回复人头像 URL */
    private String authorAvatar;

    /** 回复正文 */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** 回复状态：ACTIVE（正常）/ REMOVED（已删除） */
    private String status = "ACTIVE";

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
