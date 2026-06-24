package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

/**
 * 手艺人日程档期实体，对应数据库表 schedule_slots。
 * 用于技能交换或定制合作的空闲/忙碌时间管理。
 */
@Entity
@Data
@Table(name = "schedule_slots")
public class ScheduleSlot {
    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 手艺人用户 ID */
    private Long userId;

    /** 档期日期 */
    private LocalDate date;

    /** 档期状态：FREE（空闲）、BUSY（忙碌）、PENDING（待确认） */
    private String status = "FREE";

    /** 备注说明 */
    private String remark;
}
