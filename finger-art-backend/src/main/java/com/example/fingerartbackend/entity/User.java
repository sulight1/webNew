package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 登录账号：纯数字，唯一，不可修改 */
    @Column(unique = true)
    private String account;

    /** 昵称（展示名），可修改 */
    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String email;

    // 角色：BUYER(买家), ARTISAN(手艺人), ADMIN(管理员)
    private String role;

    /** 手作达人申请：NONE / PENDING / APPROVED / REJECTED */
    private String artisanApplyStatus = "NONE";

    /** 密码重置申请：NONE / PENDING */
    private String passwordResetStatus = "NONE";

    private String avatar;

    private String bio;

    @Column(columnDefinition = "decimal(10,2) default 0.00")
    private Double zaowuBiBalance = 0.0;

    @Column(columnDefinition = "decimal(10,2) default 0.00")
    private Double frozenBalance = 0.0;

    @Column(columnDefinition = "int default 100")
    private Integer creditScore = 100;

    @Column(columnDefinition = "decimal(3,2) default 5.00")
    private Double rating = 5.0;

    @Column(columnDefinition = "int default 0")
    private Integer reviewCount = 0;

    @Column(columnDefinition = "int default 0")
    private Integer completedOrders = 0;
}