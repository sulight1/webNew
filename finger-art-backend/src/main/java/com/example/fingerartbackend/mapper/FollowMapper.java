package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * 用户关注关系实体 {@link Follow} 的数据访问层。
 * <p>
 * 负责粉丝/关注关系的持久化，支持关注、取关及关系存在性校验。
 * </p>
 */
@Repository
public interface FollowMapper extends JpaRepository<Follow, Long> {

    /** 查询某用户关注的全部用户（关注列表） */
    List<Follow> findByFollowerId(Long followerId);

    /** 查询关注某用户的全部粉丝（粉丝列表） */
    List<Follow> findByFollowingId(Long followingId);

    /** 查询两用户之间的关注关系 */
    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    /** 取消关注：删除指定关注关系 */
    void deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);

    /** 判断是否已关注 */
    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);
}
