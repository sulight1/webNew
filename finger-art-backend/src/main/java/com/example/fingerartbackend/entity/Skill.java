package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 技能交换挂牌实体，对应数据库表 skills。
 * 用户发布可交换的手作技能及所需造物币。
 */
@Entity
@Data
@Table(name = "skills")
public class Skill {
    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 发布者用户 ID */
    private Long userId;

    /** 发布者昵称 */
    private String username;

    /** 发布者头像 URL */
    private String avatar;

    /** 技能标题 */
    private String title;

    /** 技能描述 */
    @Column(columnDefinition = "text")
    private String description;

    /** 技能分类 */
    private String category;

    /** 预计交换时长说明 */
    private String duration;

    /** 所需造物币数量 */
    private Integer zaowuBiCost;

    /** 综合评分，默认 5.0 */
    private Double rating = 5.0;

    /** 信用分，默认 100 */
    private Integer credit = 100;

    /** 累计交换次数 */
    private Integer exchangeCount = 0;

    /** 审核状态：PENDING（待审核）、APPROVED（已通过）、REJECTED（已拒绝） */
    private String status = "PENDING";
}
