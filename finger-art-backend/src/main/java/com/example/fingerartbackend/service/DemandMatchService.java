package com.example.fingerartbackend.service;

import com.example.fingerartbackend.dto.DemandMatchResult;
import com.example.fingerartbackend.entity.CustomRequest;

import java.util.List;

/**
 * 需求匹配服务接口，定义业务能力（业务服务接口）。
 */
public interface DemandMatchService {
    List<DemandMatchResult> matchArtisansForRequest(Long requestId, int limit);

    List<DemandMatchResult> matchRequestsForArtisan(Long artisanId, int limit);

    void notifyMatchedArtisans(CustomRequest request);
}
