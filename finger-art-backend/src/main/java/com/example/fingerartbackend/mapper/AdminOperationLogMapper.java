package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.AdminOperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminOperationLogMapper extends JpaRepository<AdminOperationLog, Long> {

    Page<AdminOperationLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
