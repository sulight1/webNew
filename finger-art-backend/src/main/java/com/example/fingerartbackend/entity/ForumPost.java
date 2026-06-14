package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "forum_posts")
public class ForumPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long authorId;
    private String authorName;
    private String authorAvatar;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    /** 可选配图 */
    private String imageUrl;

    private Integer viewCount = 0;
    private Integer likeCount = 0;
    private Integer replyCount = 0;

    /** ACTIVE / REMOVED */
    private String status = "ACTIVE";

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /** 当前用户是否已点赞（非持久化） */
    @Transient
    @JsonProperty("liked")
    private Boolean liked;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
