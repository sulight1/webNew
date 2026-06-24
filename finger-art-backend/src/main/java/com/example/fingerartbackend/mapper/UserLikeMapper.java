package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.UserLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 用户点赞/收藏实体 {@link UserLike} 的数据访问层。
 * <p>
 * 负责作品点赞、收藏及论坛帖子点赞等行为的持久化与统计。
 * </p>
 */
@Repository
public interface UserLikeMapper extends JpaRepository<UserLike, Long> {

    /** 查询用户对某目标的点赞/收藏记录 */
    Optional<UserLike> findByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);

    /** 批量查询用户对多个目标的点赞/收藏状态 */
    List<UserLike> findByUserIdAndTargetTypeAndTargetIdIn(Long userId, String targetType, Collection<Long> targetIds);

    /** 查询用户对某类型目标的全部点赞/收藏，按时间降序 */
    List<UserLike> findByUserIdAndTargetTypeOrderByCreateTimeDesc(Long userId, String targetType);

    /** 统计某目标的点赞/收藏总数 */
    long countByTargetTypeAndTargetId(String targetType, Long targetId);
}
