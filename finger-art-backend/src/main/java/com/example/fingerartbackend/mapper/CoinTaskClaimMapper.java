package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.CoinTaskClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface CoinTaskClaimMapper extends JpaRepository<CoinTaskClaim, Long> {
    boolean existsByUserIdAndTaskCodeAndClaimDate(Long userId, String taskCode, LocalDate claimDate);
    boolean existsByUserIdAndTaskCodeAndReferenceId(Long userId, String taskCode, Long referenceId);
    Optional<CoinTaskClaim> findByUserIdAndTaskCodeAndClaimDate(Long userId, String taskCode, LocalDate claimDate);
}
