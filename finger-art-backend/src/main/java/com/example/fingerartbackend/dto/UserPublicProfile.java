package com.example.fingerartbackend.dto;

import lombok.Data;

@Data
public class UserPublicProfile {
    private Long id;
    private String username;
    private String role;
    private String avatar;
    private String bio;
    private Double rating;
    private Integer reviewCount;
    private Integer completedOrders;
    private Integer creditScore;
    private long followerCount;
    private long followingCount;
    private long productCount;
    private long skillCount;
}
