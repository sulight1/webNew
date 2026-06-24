package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 技能交换订单实体，对应数据库表 skill_exchanges。
 * 记录双方预约、确认、完成及爽约等交换流程。
 */
@Entity
@Data
@Table(name = "skill_exchanges")
public class SkillExchange {
    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 发起方（请求交换的用户） */
    @ManyToOne
    private User userA;

    /** 提供方（技能提供者） */
    @ManyToOne
    private User userB;

    /** 交换状态：REQUESTED、ACCEPTED、CONFIRMED、COMPLETED、NO_SHOW、CANCELLED */
    private String status;

    /** 交换说明/备注 */
    private String description;

    /** 消耗造物币数量 */
    private Integer zaowuBiCost;

    /** 预约日期 */
    private LocalDate scheduleDate;

    /** 发起方是否已确认完成 */
    private Boolean userAConfirmed = false;

    /** 提供方是否已确认完成 */
    private Boolean userBConfirmed = false;

    /** 发起方是否已评价 */
    private Boolean userAReviewed = false;

    /** 提供方是否已评价 */
    private Boolean userBReviewed = false;

    /** 创建时间 */
    private LocalDateTime createdAt = LocalDateTime.now();
}
