package com.example.fingerartbackend.service;

import com.example.fingerartbackend.dto.DemandMatchResult;
import com.example.fingerartbackend.entity.CustomRequest;

import java.util.List;

public interface DemandMatchService {
    List<DemandMatchResult> matchArtisansForRequest(Long requestId, int limit);

    List<DemandMatchResult> matchRequestsForArtisan(Long artisanId, int limit);

    void notifyMatchedArtisans(CustomRequest request);
}
