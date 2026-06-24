package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * 私信消息实体，对应数据库表 messages。
 * 用户间一对一站内信通信。
 */
@Entity
@Data
@Table(name = "messages")
public class Message {
    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 发送者用户 ID */
    private Long senderId;

    /** 发送者昵称 */
    private String senderName;

    /** 接收者用户 ID */
    private Long receiverId;

    /** 接收者昵称 */
    private String receiverName;

    /** 消息正文 */
    @Column(columnDefinition = "text")
    private String content;

    /** 是否已读 */
    @JsonProperty("isRead")
    private boolean isRead = false;

    /** 发送时间 */
    private LocalDateTime createTime;

    /**
     * JPA 持久化前回调，自动写入创建时间。
     */
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
