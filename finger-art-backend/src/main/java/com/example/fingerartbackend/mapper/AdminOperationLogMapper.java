package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.AdminOperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 管理员操作日志实体 {@link AdminOperationLog} 的数据访问层。
 * <p>
 * 记录管理员在后台的关键操作，支持分页审计查询。
 * </p>
 */
@Repository
public interface AdminOperationLogMapper extends JpaRepository<AdminOperationLog, Long> {

    /** 分页查询全部操作日志，按创建时间降序 */
    Page<AdminOperationLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
