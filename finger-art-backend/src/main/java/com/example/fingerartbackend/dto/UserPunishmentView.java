package com.example.fingerartbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户处罚视图 DTO。
 * 用于向用户或管理员展示当前生效的处罚详情。
 */
@Data
public class UserPunishmentView {
    /** 处罚记录 ID */
    private Long id;

    /** 处罚类型编码：ACCOUNT_BAN / NO_ORDER / NO_FORUM / NO_PRODUCT / NO_SKILL */
    private String type;

    /** 处罚类型中文标签 */
    private String typeLabel;

    /** 处罚开始时间 */
    private LocalDateTime startAt;

    /** 处罚结束时间；永久处罚时有值但 permanent 为 true */
    private LocalDateTime endAt;

    /** 处罚原因 */
    private String reason;

    /** 是否为永久处罚 */
    private boolean permanent;
}
