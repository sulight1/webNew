package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.entity.ContentReport;
import com.example.fingerartbackend.service.AdminAuditService;
import com.example.fingerartbackend.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private AdminAuditService adminAuditService;

    @PostMapping
    public Result<ContentReport> submit(@RequestBody ContentReport report) {
        return Result.success(reportService.submitReport(report));
    }

    @GetMapping
    public Result<List<ContentReport>> list(@RequestParam(required = false) String status) {
        return Result.success(reportService.listReports(status));
    }

    @GetMapping("/pending-count")
    public Result<Long> pendingCount() {
        return Result.success(reportService.countPending());
    }

    @PatchMapping("/{id}/handle")
    public Result<ContentReport> handle(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long handlerId = Long.valueOf(body.get("handlerId").toString());
        String action = body.get("action").toString();
        String note = body.get("handleNote") != null ? body.get("handleNote").toString() : "";
        ContentReport report = reportService.handleReport(id, handlerId, action, note);
        adminAuditService.log("HANDLE_REPORT", "REPORT", id,
                "处理举报：" + action + "，目标 " + report.getTargetType() + " #" + report.getTargetId());
        return Result.success(report);
    }
}
