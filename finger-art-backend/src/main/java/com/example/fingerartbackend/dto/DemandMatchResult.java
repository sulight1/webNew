package com.example.fingerartbackend.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 需求/手艺人智能匹配结果 DTO。
 * 用于双向推荐：为买家推荐手艺人，或为手艺人推荐需求。
 */
@Data
public class DemandMatchResult {
    /** 推荐手艺人用户 ID */
    private Long artisanId;

    /** 手艺人昵称 */
    private String username;

    /** 手艺人头像 URL */
    private String avatar;

    /** 手艺人简介 */
    private String bio;

    /** 综合评分 */
    private Double rating;

    /** 信用分 */
    private Integer creditScore;

    /** 已完成订单数 */
    private Integer completedOrders;

    /** 匹配得分 */
    private int score;

    /** 匹配理由列表 */
    private List<String> reasons = new ArrayList<>();

    /** 推荐需求 ID（面向手艺人推荐时使用） */
    private Long requestId;

    /** 推荐需求标题 */
    private String requestTitle;

    /** 推荐需求分类 */
    private String requestCategory;

    /** 需求预算下限 */
    private Double budgetMin;

    /** 需求预算上限 */
    private Double budgetMax;

    /** 需求期望交付期限 */
    private String deadline;
}
