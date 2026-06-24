package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.UserPunishment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户处罚记录实体 {@link UserPunishment} 的数据访问层。
 * <p>
 * 负责账号封禁、禁言、禁下单等处罚记录的持久化与生效状态查询。
 * </p>
 */
@Repository
public interface UserPunishmentMapper extends JpaRepository<UserPunishment, Long> {

    /** 查询用户当前仍生效的处罚记录（未过期或无结束时间） */
    @Query("SELECT p FROM UserPunishment p WHERE p.userId = :userId AND (p.endAt IS NULL OR p.endAt > :now) ORDER BY p.createdAt DESC")
    List<UserPunishment> findActiveByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /** 按用户 ID 与处罚类型查询历史记录 */
    List<UserPunishment> findByUserIdAndType(Long userId, String type);

    /** 删除某用户指定类型的处罚记录 */
    void deleteByUserIdAndType(Long userId, String type);
}
