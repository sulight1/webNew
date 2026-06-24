package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 手作作品实体，对应数据库表 products。
 * 用于市集展示、上架审核及订单关联。
 */
@Entity
@Data
@Table(name = "products")
public class Product {
    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 作品标题 */
    private String title;

    /** 售价（元） */
    private Double price;

    /** 作品类型：READY_MADE（成品）或 CUSTOMIZABLE（可定制） */
    private String type;

    /** 创作者用户 ID */
    private Long creatorId;

    /** 创作者昵称（冗余展示） */
    private String creator;

    /** 创作者头像 URL */
    private String creatorAvatar;

    /** 封面图 URL */
    private String image;

    /** 点赞数 */
    @Column(columnDefinition = "int default 0")
    private Integer likes = 0;

    /** 库存：成品默认 1，可定制默认 999 */
    @Column(columnDefinition = "int default 1")
    private Integer stock = 1;

    /** 作品分类：crochet（钩织）、resin（滴胶）、nails（穿戴甲）、clay（粘土）、flower（缠花）等 */
    private String category;

    /** 制作工艺名称，关联 craft_techniques 表 */
    private String craftTechnique;

    /** 作品描述 */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** JSON 数组：细节图 URL 列表 */
    @Column(columnDefinition = "TEXT")
    private String detailImages;

    /** JSON 数组：制作过程/花絮图 URL 列表 */
    @Column(columnDefinition = "TEXT")
    private String processImages;

    /** 审核状态：PENDING（待审核）、APPROVED（已通过）、REJECTED（已拒绝） */
    private String status = "PENDING";

    /** 曝光加权（造物币推广） */
    @Column(columnDefinition = "int default 0")
    private Integer exposureBoost = 0;

    /** 推广加权截止时间 */
    private java.time.LocalDateTime boostUntil;

    /** 当前用户是否已点赞（非持久化） */
    @Transient
    @JsonProperty("liked")
    private Boolean liked;

    /** 当前用户是否已收藏（非持久化） */
    @Transient
    @JsonProperty("favorited")
    private Boolean favorited;
}
