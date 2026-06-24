package com.example.fingerartbackend.dto;

import lombok.Data;

/**
 * 物流轨迹节点 DTO。
 * 表示快递在某一时刻的状态描述。
 */
@Data
public class LogisticsTrackItem {
    /** 轨迹时间 */
    private String time;

    /** 轨迹描述（如「已签收」） */
    private String context;
}
