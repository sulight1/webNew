package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Data
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private Double price;
    private String type; // READY_MADE 或 CUSTOMIZABLE
    private Long creatorId;
    private String creator;
    private String creatorAvatar;
    private String image;
    
    @Column(columnDefinition = "int default 0")
    private Integer likes = 0;

    /** 库存：成品默认 1，可定制默认 999 */
    @Column(columnDefinition = "int default 1")
    private Integer stock = 1;

    // 作品分类：crochet(钩织), resin(滴胶), nails(穿戴甲), clay(粘土), flower(缠花)
    private String category;

    // 制作工艺：关联分类下的具体工艺名称
    private String craftTechnique;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** JSON 数组：细节图 URL 列表 */
    @Column(columnDefinition = "TEXT")
    private String detailImages;

    /** JSON 数组：制作过程/花絮图 URL 列表 */
    @Column(columnDefinition = "TEXT")
    private String processImages;

    // 状态：PENDING(待审核), APPROVED(已通过), REJECTED(已拒绝)
    private String status = "PENDING";

    /** 曝光加权（造物币推广） */
    @Column(columnDefinition = "int default 0")
    private Integer exposureBoost = 0;

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