package com.example.fingerartbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CustomRequestBidView {
    private Long id;
    private Long requestId;
    private Long artisanId;
    private String artisanUsername;
    private String artisanAvatar;
    private Double rating;
    private Integer creditScore;
    private Integer reviewCount;
    private String message;
    private String status;
    private LocalDateTime createTime;
}
