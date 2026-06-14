package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.dto.DemandMatchResult;
import com.example.fingerartbackend.service.DemandMatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/demand-match")
public class DemandMatchController {

    @Autowired
    private DemandMatchService demandMatchService;

    @GetMapping("/request/{requestId}")
    public Result<List<DemandMatchResult>> matchForRequest(
            @PathVariable Long requestId,
            @RequestParam(defaultValue = "8") int limit) {
        return Result.success(demandMatchService.matchArtisansForRequest(requestId, limit));
    }

    @GetMapping("/artisan/{artisanId}")
    public Result<List<DemandMatchResult>> matchForArtisan(
            @PathVariable Long artisanId,
            @RequestParam(defaultValue = "8") int limit) {
        return Result.success(demandMatchService.matchRequestsForArtisan(artisanId, limit));
    }
}
