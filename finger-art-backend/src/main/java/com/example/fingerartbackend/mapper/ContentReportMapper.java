package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.ContentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 内容举报实体 {@link ContentReport} 的数据访问层。
 * <p>
 * 负责用户举报记录的持久化，支持管理员按状态审核与统计。
 * </p>
 */
@Repository
public interface ContentReportMapper extends JpaRepository<ContentReport, Long> {

    /** 按处理状态查询举报，按创建时间降序 */
    List<ContentReport> findByStatusOrderByCreatedAtDesc(String status);

    /** 查询全部举报，按创建时间降序 */
    List<ContentReport> findAllByOrderByCreatedAtDesc();

    /** 统计指定状态的举报数量 */
    long countByStatus(String status);
}
