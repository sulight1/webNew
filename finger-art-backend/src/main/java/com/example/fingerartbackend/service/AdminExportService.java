package com.example.fingerartbackend.service;

/**
 * 管理端服务接口，定义业务能力（业务服务接口）。
 */
public interface AdminExportService {
    byte[] exportUsersExcel();

    byte[] exportOrdersExcel();
}
