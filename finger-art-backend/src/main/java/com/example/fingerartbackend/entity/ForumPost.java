package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * 社区帖子实体，对应数据库表 forum_posts。
 * 用于论坛发帖、浏览、点赞与回复统计。
 */
@Entity
@Data
@Table(name = "forum_posts")
public class ForumPost {
    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 作者用户 ID */
    private Long authorId;

    /** 作者昵称 */
    private String authorName;

    /** 作者头像 URL */
    private String authorAvatar;

    /** 帖子标题 */
    private String title;

    /** 帖子正文 */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** 可选配图 URL */
    private String imageUrl;

    /** 浏览次数 */
    private Integer viewCount = 0;

    /** 点赞数 */
    private Integer likeCount = 0;

    /** 回复数 */
    private Integer replyCount = 0;

    /** 帖子状态：ACTIVE（正常）/ REMOVED（已下架） */
    private String status = "ACTIVE";

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 最后更新时间 */
    private LocalDateTime updateTime;

    /** 当前用户是否已点赞（非持久化） */
    @Transient
    @JsonProperty("liked")
    private Boolean liked;

    /**
     * JPA 持久化前回调，自动写入创建时间。
     */
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    /**
     * JPA 更新前回调，自动刷新更新时间。
     */
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
