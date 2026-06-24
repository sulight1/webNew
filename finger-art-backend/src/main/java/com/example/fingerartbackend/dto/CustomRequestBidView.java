package com.example.fingerartbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 定制需求竞标视图 DTO。
 * 展示竞标记录及手艺人公开信用信息，供买家选人参考。
 */
@Data
public class CustomRequestBidView {
    /** 竞标记录 ID */
    private Long id;

    /** 关联定制需求 ID */
    private Long requestId;

    /** 竞标手艺人用户 ID */
    private Long artisanId;

    /** 手艺人昵称 */
    private String artisanUsername;

    /** 手艺人头像 URL */
    private String artisanAvatar;

    /** 手艺人综合评分 */
    private Double rating;

    /** 手艺人信用分 */
    private Integer creditScore;

    /** 手艺人评价总数 */
    private Integer reviewCount;

    /** 竞标留言/方案说明 */
    private String message;

    /** 竞标状态：PENDING / SELECTED / REJECTED */
    private String status;

    /** 提交时间 */
    private LocalDateTime createTime;
}
