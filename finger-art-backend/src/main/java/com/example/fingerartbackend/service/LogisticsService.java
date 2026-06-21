package com.example.fingerartbackend.service;

import com.example.fingerartbackend.dto.LogisticsTraceResult;

public interface LogisticsService {
    LogisticsTraceResult queryOrderLogistics(Long orderId, Long userId);
}
