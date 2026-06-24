package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.CoinTaskClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 金币任务领取记录实体 {@link CoinTaskClaim} 的数据访问层。
 * <p>
 * 负责每日任务、一次性任务领取记录的持久化，防止重复领取。
 * </p>
 */
@Repository
public interface CoinTaskClaimMapper extends JpaRepository<CoinTaskClaim, Long> {

    /** 判断用户在某日是否已领取指定任务 */
    boolean existsByUserIdAndTaskCodeAndClaimDate(Long userId, String taskCode, LocalDate claimDate);

    /** 判断用户是否已针对某关联对象领取过指定任务（如首单奖励） */
    boolean existsByUserIdAndTaskCodeAndReferenceId(Long userId, String taskCode, Long referenceId);

    /** 查询用户在某日对指定任务的领取记录 */
    Optional<CoinTaskClaim> findByUserIdAndTaskCodeAndClaimDate(Long userId, String taskCode, LocalDate claimDate);
}
