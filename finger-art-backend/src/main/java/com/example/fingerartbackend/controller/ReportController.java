package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.entity.ContentReport;
import com.example.fingerartbackend.service.AdminAuditService;
import com.example.fingerartbackend.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 内容举报控制器。
 * 处理用户举报提交、列表查询及管理员处置，对应社区风控与审核模块。
 */
@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private AdminAuditService adminAuditService;

    /**
     * 提交内容举报。
     *
     * @param report 举报实体（目标类型、目标 ID、原因等）
     * @return 保存后的举报记录
     */
    @PostMapping
    public Result<ContentReport> submit(@RequestBody ContentReport report) {
        return Result.success(reportService.submitReport(report));
    }

    /**
     * 查询举报列表，可按状态筛选。
     *
     * @param status 可选举报状态（如 PENDING、RESOLVED）
     * @return 举报记录列表
     */
    @GetMapping
    public Result<List<ContentReport>> list(@RequestParam(required = false) String status) {
        return Result.success(reportService.listReports(status));
    }

    /**
     * 统计待处理举报数量。
     *
     * @return 待处理举报条数
     */
    @GetMapping("/pending-count")
    public Result<Long> pendingCount() {
        return Result.success(reportService.countPending());
    }

    /**
     * 管理员处理指定举报。
     *
     * @param id   举报 ID
     * @param body 含 handlerId、action、handleNote 的请求体
     * @return 处理后的举报记录
     */
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
