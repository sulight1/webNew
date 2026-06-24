package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.CustomRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 定制需求实体 {@link CustomRequest} 的数据访问层。
 * <p>
 * 支持定制需求的 CRUD、动态条件筛选及按买家/状态统计。
 * </p>
 */
@Repository
public interface CustomRequestMapper extends JpaRepository<CustomRequest, Long>, JpaSpecificationExecutor<CustomRequest> {

    /** 按买家 ID 查询其发布的定制需求 */
    List<CustomRequest> findByBuyerId(Long buyerId);

    /** 统计指定状态的定制需求数量 */
    long countByStatus(String status);
}
