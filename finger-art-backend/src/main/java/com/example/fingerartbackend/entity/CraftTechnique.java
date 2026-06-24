package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 制作工艺字典实体，对应数据库表 craft_techniques。
 * 按作品分类维护可选工艺名称与展示标签。
 */
@Entity
@Data
@Table(name = "craft_techniques", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"category", "name"})
})
/**
 * 工艺技法实体，对应数据库表及 JPA 映射。
 */
public class CraftTechnique {
    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属作品分类：crochet / resin / nails / clay / flower / perler / embroidery / bead / leather / wood / candle / paper 等 */
    @Column(nullable = false)
    private String category;

    /** 工艺标识，存储到 product.craftTechnique */
    @Column(nullable = false)
    private String name;

    /** 前端展示标签 */
    @Column(nullable = false)
    private String label;
}
