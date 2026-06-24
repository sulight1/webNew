package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.OrderMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 订单里程碑实体 {@link OrderMilestone} 的数据访问层。
 * <p>
 * 记录定制订单制作进度节点，支持按订单查询时间线。
 * </p>
 */
@Repository
public interface OrderMilestoneMapper extends JpaRepository<OrderMilestone, Long> {

    /** 按订单 ID 查询里程碑，按创建时间升序排列 */
    List<OrderMilestone> findByOrderIdOrderByCreatedAtAsc(Long orderId);
}
