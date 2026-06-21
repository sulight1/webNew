package com.example.fingerartbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserPunishmentView {
    private Long id;
    private String type;
    private String typeLabel;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String reason;
    private boolean permanent;
}
