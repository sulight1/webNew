package com.example.fingerartbackend.service;

import com.example.fingerartbackend.entity.ContentReport;

import java.util.List;
import java.util.Map;

/**
 * 举报服务接口，定义业务能力（业务服务接口）。
 */
public interface ReportService {
    ContentReport submitReport(ContentReport report);
    List<ContentReport> listReports(String status);
    ContentReport handleReport(Long id, Long handlerId, String action, String handleNote);
    long countPending();
}
