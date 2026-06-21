package com.example.fingerartbackend.service;

public interface AdminExportService {
    byte[] exportUsersExcel();

    byte[] exportOrdersExcel();
}
