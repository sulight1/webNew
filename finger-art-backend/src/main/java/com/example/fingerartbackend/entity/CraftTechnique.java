package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "craft_techniques", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"category", "name"})
})
public class CraftTechnique {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 所属作品分类：crochet / resin / nails / clay / flower / perler / embroidery / bead / leather / wood / candle / paper
    @Column(nullable = false)
    private String category;

    // 工艺标识（存储到 product.craftTechnique）
    @Column(nullable = false)
    private String name;

    // 显示标签
    @Column(nullable = false)
    private String label;
}
