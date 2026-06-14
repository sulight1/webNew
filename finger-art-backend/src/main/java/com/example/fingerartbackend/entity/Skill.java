package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "skills")
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String username;
    private String avatar;
    private String title;
    @Column(columnDefinition = "text")
    private String description;
    private String category;
    private String duration;
    private Integer zaowuBiCost;
    private Double rating = 5.0;
    private Integer credit = 100;
    private Integer exchangeCount = 0;

    // 状态：PENDING(待审核), APPROVED(已通过), REJECTED(已拒绝)
    private String status = "PENDING";
}