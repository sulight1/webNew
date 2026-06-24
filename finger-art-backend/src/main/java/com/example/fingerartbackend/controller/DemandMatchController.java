package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.dto.DemandMatchResult;
import com.example.fingerartbackend.service.DemandMatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 需求匹配控制器。
 * 为定制需求推荐合适手作人，或为手作人推荐可接单需求，对应智能匹配模块。
 */
@RestController
@RequestMapping("/demand-match")
public class DemandMatchController {

    @Autowired
    private DemandMatchService demandMatchService;

    /**
     * 为指定定制需求匹配推荐手作人。
     *
     * @param requestId 定制需求 ID
     * @param limit     返回结果数量上限，默认 8
     * @return 匹配的手作人列表及匹配得分
     */
    @GetMapping("/request/{requestId}")
    public Result<List<DemandMatchResult>> matchForRequest(
            @PathVariable Long requestId,
            @RequestParam(defaultValue = "8") int limit) {
        return Result.success(demandMatchService.matchArtisansForRequest(requestId, limit));
    }

    /**
     * 为指定手作人匹配可接单的定制需求。
     *
     * @param artisanId 手作人用户 ID
     * @param limit     返回结果数量上限，默认 8
     * @return 匹配的需求列表及匹配得分
     */
    @GetMapping("/artisan/{artisanId}")
    public Result<List<DemandMatchResult>> matchForArtisan(
            @PathVariable Long artisanId,
            @RequestParam(defaultValue = "8") int limit) {
        return Result.success(demandMatchService.matchRequestsForArtisan(artisanId, limit));
    }
}
