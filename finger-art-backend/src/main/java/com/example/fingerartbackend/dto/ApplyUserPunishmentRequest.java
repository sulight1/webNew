package com.example.fingerartbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class ApplyUserPunishmentRequest {
    private List<String> types;
    /** 处罚时长（小时），永久处罚时为 null */
    private Integer durationHours;
    private Boolean permanent;
    private String reason;
}
