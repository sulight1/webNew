package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.fingerartbackend.dto.UserPunishmentView;
import lombok.Data;

import java.util.List;

/**
 * 平台用户实体，对应数据库表 users。
 * 存储账号、角色、钱包余额、信用评分及收货信息等核心用户数据。
 */
@Entity
@Data
@Table(name = "users")
public class User {
    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 登录账号：纯数字，唯一，不可修改 */
    @Column(unique = true)
    private String account;

    /** 昵称（展示名），可修改 */
    @Column(nullable = false)
    private String username;

    /** 登录密码（仅写入，不对外返回） */
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /** 邮箱地址 */
    private String email;

    /** 用户角色：BUYER（买家）、ARTISAN（手艺人）、ADMIN（管理员） */
    private String role;

    /** 手作达人申请状态：NONE / PENDING / APPROVED / REJECTED */
    private String artisanApplyStatus = "NONE";

    /** 密码重置申请状态：NONE / PENDING */
    private String passwordResetStatus = "NONE";

    /** 头像 URL */
    private String avatar;

    /** 个人简介 */
    private String bio;

    /** 收货人姓名 */
    private String shippingName;

    /** 收货人手机 */
    private String shippingPhone;

    /** 收货详细地址 */
    @Column(columnDefinition = "TEXT")
    private String shippingAddress;

    /** 造物币可用余额 */
    @Column(columnDefinition = "decimal(10,2) default 0.00")
    private Double zaowuBiBalance = 0.0;

    /** 冻结余额（订单托管等场景） */
    @Column(columnDefinition = "decimal(10,2) default 0.00")
    private Double frozenBalance = 0.0;

    /** 信用分，默认 100 */
    @Column(columnDefinition = "int default 100")
    private Integer creditScore = 100;

    /** 综合评分，默认 5.0 */
    @Column(columnDefinition = "decimal(3,2) default 5.00")
    private Double rating = 5.0;

    /** 收到评价总数 */
    @Column(columnDefinition = "int default 0")
    private Integer reviewCount = 0;

    /** 已完成订单数 */
    @Column(columnDefinition = "int default 0")
    private Integer completedOrders = 0;

    /** 管理员 TOTP 密钥（Base32），仅服务端使用 */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String totpSecret;

    /** 是否已启用 TOTP 二次验证（仅管理员） */
    @Column(columnDefinition = "boolean default false")
    private Boolean totpEnabled = false;

    /** 当前生效的处罚列表（非持久化，查询时填充） */
    @Transient
    private List<UserPunishmentView> activePunishments;
}
