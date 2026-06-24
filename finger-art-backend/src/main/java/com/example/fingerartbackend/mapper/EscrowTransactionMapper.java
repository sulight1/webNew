package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.EscrowTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 托管交易流水实体 {@link EscrowTransaction} 的数据访问层。
 * <p>
 * 记录订单资金托管、释放、退款等状态变更流水。
 * </p>
 */
@Repository
public interface EscrowTransactionMapper extends JpaRepository<EscrowTransaction, Long> {

    /** 按订单 ID 查询托管流水，按创建时间升序 */
    List<EscrowTransaction> findByOrderIdOrderByCreatedAtAsc(Long orderId);
}
