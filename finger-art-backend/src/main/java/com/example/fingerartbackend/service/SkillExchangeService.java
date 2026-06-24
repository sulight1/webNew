package com.example.fingerartbackend.service;

import com.example.fingerartbackend.entity.SkillExchange;

import java.util.List;

/**
 * 技能服务接口，定义业务能力（业务服务接口）。
 */
public interface SkillExchangeService {
    SkillExchange requestExchange(Long userAId, Long userBId, String description, Integer cost, String scheduleDate);
    SkillExchange acceptExchange(Long exchangeId, Long userId);
    SkillExchange confirmExchange(Long exchangeId, Long userId);
    SkillExchange completeExchange(Long exchangeId, Long userId);
    SkillExchange reportNoShow(Long exchangeId, Long reporterId);
    List<SkillExchange> getMyExchanges(Long userId);
    void processOverdueExchanges();
}
