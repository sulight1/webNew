package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 评价实体 {@link Review} 的数据访问层。
 * <p>
 * 负责订单/交换评价的记录与查询，支持防重复评价校验。
 * </p>
 */
@Repository
public interface ReviewMapper extends JpaRepository<Review, Long> {

    /** 按被评价用户 ID 查询收到的评价，按创建时间降序 */
    List<Review> findByToUserIdOrderByCreatedAtDesc(Long toUserId);

    /** 按作品 ID 查询评价列表，按创建时间降序 */
    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);

    /** 查询某用户对某订单的评价 */
    Optional<Review> findByOrderIdAndFromUserId(Long orderId, Long fromUserId);

    /** 查询某用户对某技能交换的评价 */
    Optional<Review> findByExchangeIdAndFromUserId(Long exchangeId, Long fromUserId);

    /** 判断某用户是否已对某订单评价 */
    boolean existsByOrderIdAndFromUserId(Long orderId, Long fromUserId);

    /** 判断某用户是否已对某技能交换评价 */
    boolean existsByExchangeIdAndFromUserId(Long exchangeId, Long fromUserId);
}
