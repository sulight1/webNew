package com.example.fingerartbackend.service;

import com.example.fingerartbackend.dto.LogisticsTraceResult;

/**
 * 物流服务接口，定义业务能力（业务服务接口）。
 */
public interface LogisticsService {
    LogisticsTraceResult queryOrderLogistics(Long orderId, Long userId);
}
