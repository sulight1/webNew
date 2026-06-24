package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.service.AdminAuditService;
import com.example.fingerartbackend.service.AdminExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 管理员数据导出控制器。
 * 提供用户、订单等业务数据的 Excel 导出，并记录导出审计日志。
 */
@RestController
@RequestMapping("/admin/export")
public class AdminExportController {

    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Autowired
    private AdminExportService adminExportService;

    @Autowired
    private AdminAuditService adminAuditService;

    /**
     * 导出用户列表为 Excel 文件。
     *
     * @return Excel 文件字节流，文件名含导出时间戳
     */
    @GetMapping("/users")
    public ResponseEntity<byte[]> exportUsers() {
        byte[] data = adminExportService.exportUsersExcel();
        String filename = "指尖造物_用户_" + FILE_TS.format(LocalDateTime.now()) + ".xlsx";
        adminAuditService.log("EXPORT_USERS", "EXPORT", null, "导出用户 Excel");
        return excelResponse(data, filename);
    }

    /**
     * 导出订单列表为 Excel 文件。
     *
     * @return Excel 文件字节流，文件名含导出时间戳
     */
    @GetMapping("/orders")
    public ResponseEntity<byte[]> exportOrders() {
        byte[] data = adminExportService.exportOrdersExcel();
        String filename = "指尖造物_订单_" + FILE_TS.format(LocalDateTime.now()) + ".xlsx";
        adminAuditService.log("EXPORT_ORDERS", "EXPORT", null, "导出订单 Excel");
        return excelResponse(data, filename);
    }

    /**
     * 构建 Excel 下载响应，设置 Content-Disposition 与 MIME 类型。
     *
     * @param data     Excel 文件字节内容
     * @param filename 原始文件名（中文需 URL 编码）
     * @return 带附件头的 HTTP 响应
     */
    private ResponseEntity<byte[]> excelResponse(byte[] data, String filename) {
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }
}
