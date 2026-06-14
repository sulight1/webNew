package com.example.fingerartbackend.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DemandMatchResult {
    private Long artisanId;
    private String username;
    private String avatar;
    private String bio;
    private Double rating;
    private Integer creditScore;
    private Integer completedOrders;
    private int score;
    private List<String> reasons = new ArrayList<>();

    // 面向手作人的推荐需求字段
    private Long requestId;
    private String requestTitle;
    private String requestCategory;
    private Double budgetMin;
    private Double budgetMax;
    private String deadline;
}
