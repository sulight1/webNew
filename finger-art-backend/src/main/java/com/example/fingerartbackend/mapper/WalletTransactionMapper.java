package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletTransactionMapper extends JpaRepository<WalletTransaction, Long> {
    Page<WalletTransaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    java.util.Optional<WalletTransaction> findByOutTradeNoAndUserId(String outTradeNo, Long userId);
}
