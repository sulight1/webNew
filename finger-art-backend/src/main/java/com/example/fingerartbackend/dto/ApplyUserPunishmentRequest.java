package com.example.fingerartbackend.dto;

import lombok.Data;

import java.util.List;

/**
 * 施加用户处罚请求 DTO。
 * 管理员对用户实施一项或多项限制。
 */
@Data
public class ApplyUserPunishmentRequest {
    /** 处罚类型列表：ACCOUNT_BAN / NO_ORDER / NO_FORUM / NO_PRODUCT / NO_SKILL */
    private List<String> types;

    /** 处罚时长（小时）；永久处罚时为 null */
    private Integer durationHours;

    /** 是否永久处罚 */
    private Boolean permanent;

    /** 处罚原因 */
    private String reason;
}
