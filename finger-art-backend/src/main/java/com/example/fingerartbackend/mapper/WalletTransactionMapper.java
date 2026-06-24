package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 钱包流水实体 {@link WalletTransaction} 的数据访问层。
 * <p>
 * 负责用户金币充值、消费等流水记录的持久化与分页查询。
 * </p>
 */
@Repository
public interface WalletTransactionMapper extends JpaRepository<WalletTransaction, Long> {

    /** 分页查询某用户的流水记录，按创建时间降序 */
    Page<WalletTransaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /** 按商户订单号与用户 ID 查询流水（支付回调幂等校验） */
    java.util.Optional<WalletTransaction> findByOutTradeNoAndUserId(String outTradeNo, Long userId);
}
