package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 敏感词实体，对应数据库表 sensitive_words。
 * 用于内容审核时的违禁词过滤。
 */
@Entity
@Data
@Table(name = "sensitive_words")
public class SensitiveWord {
    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 敏感词文本，全局唯一 */
    @Column(unique = true, nullable = false)
    private String word;

    /** 是否启用 */
    @Column(columnDefinition = "boolean default true")
    private Boolean enabled = true;
}
