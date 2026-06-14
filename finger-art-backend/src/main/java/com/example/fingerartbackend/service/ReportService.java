package com.example.fingerartbackend.service;

import com.example.fingerartbackend.entity.ContentReport;

import java.util.List;
import java.util.Map;

public interface ReportService {
    ContentReport submitReport(ContentReport report);
    List<ContentReport> listReports(String status);
    ContentReport handleReport(Long id, Long handlerId, String action, String handleNote);
    long countPending();
}
