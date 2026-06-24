package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.CustomOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 定制订单实体 {@link CustomOrder} 的数据访问层。
 * <p>
 * 负责买家/匠人订单列表、状态统计及重复下单校验等查询。
 * </p>
 */
@Repository
public interface CustomOrderMapper extends JpaRepository<CustomOrder, Long> {

    /** 按匠人 ID 查询其承接的订单 */
    List<CustomOrder> findByArtisanId(Long artisanId);

    /** 按买家 ID 查询其发起的订单 */
    List<CustomOrder> findByBuyerId(Long buyerId);

    /** 按买家 ID 查询订单，按创建时间降序 */
    List<CustomOrder> findByBuyerIdOrderByCreateTimeDesc(Long buyerId);

    /** 查询买家对某作品在指定状态下的订单，用于防重复下单 */
    List<CustomOrder> findByBuyerIdAndProductIdAndStatusOrderByCreateTimeDesc(Long buyerId, Long productId, String status);

    /** 按订单状态查询，按创建时间降序 */
    List<CustomOrder> findByStatusOrderByCreateTimeDesc(String status);

    /** 统计指定状态的订单数量 */
    long countByStatus(String status);
}
