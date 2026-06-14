package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.ContentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentReportMapper extends JpaRepository<ContentReport, Long> {
    List<ContentReport> findByStatusOrderByCreatedAtDesc(String status);
    List<ContentReport> findAllByOrderByCreatedAtDesc();
    long countByStatus(String status);
}
