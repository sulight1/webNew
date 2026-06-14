package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.EscrowTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EscrowTransactionMapper extends JpaRepository<EscrowTransaction, Long> {
    List<EscrowTransaction> findByOrderIdOrderByCreatedAtAsc(Long orderId);
}
