package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.CustomRequestBid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 定制需求报价实体 {@link CustomRequestBid} 的数据访问层。
 * <p>
 * 负责匠人投标记录的持久化及按需求/匠人维度的查询。
 * </p>
 */
@Repository
public interface CustomRequestBidMapper extends JpaRepository<CustomRequestBid, Long> {

    /** 按需求 ID 查询全部报价，按创建时间降序 */
    List<CustomRequestBid> findByRequestIdOrderByCreateTimeDesc(Long requestId);

    /** 查询某匠人对某需求的报价（每人每需求仅一条） */
    Optional<CustomRequestBid> findByRequestIdAndArtisanId(Long requestId, Long artisanId);

    /** 按匠人 ID 查询其全部报价记录，按创建时间降序 */
    List<CustomRequestBid> findByArtisanIdOrderByCreateTimeDesc(Long artisanId);

    /** 统计某需求在指定状态下的报价数量 */
    long countByRequestIdAndStatus(Long requestId, String status);
}
