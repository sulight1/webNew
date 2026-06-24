package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.entity.AdminOperationLog;
import com.example.fingerartbackend.service.AdminAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员审计控制器。
 * 提供管理员操作日志的分页查询，对应后台审计与合规模块。
 */
@RestController
@RequestMapping("/admin")
public class AdminAuditController {

    @Autowired
    private AdminAuditService adminAuditService;

    /**
     * 分页查询管理员操作日志。
     *
     * @param page 页码，从 0 开始
     * @param size 每页条数
     * @return 操作日志分页数据
     */
    @GetMapping("/operation-logs")
    public Result<Page<AdminOperationLog>> listOperationLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(adminAuditService.listLogs(page, size));
    }
}
