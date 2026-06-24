package com.example.fingerartbackend.dto;

import lombok.Data;

/**
 * 用户公开资料 DTO。
 * 用于个人主页展示，不含敏感信息（密码、余额等）。
 */
@Data
public class UserPublicProfile {
    /** 用户 ID */
    private Long id;

    /** 昵称 */
    private String username;

    /** 角色：BUYER / ARTISAN / ADMIN */
    private String role;

    /** 头像 URL */
    private String avatar;

    /** 个人简介 */
    private String bio;

    /** 综合评分 */
    private Double rating;

    /** 收到评价总数 */
    private Integer reviewCount;

    /** 已完成订单数 */
    private Integer completedOrders;

    /** 信用分 */
    private Integer creditScore;

    /** 粉丝数 */
    private long followerCount;

    /** 关注数 */
    private long followingCount;

    /** 上架作品数 */
    private long productCount;

    /** 发布技能数 */
    private long skillCount;
}
