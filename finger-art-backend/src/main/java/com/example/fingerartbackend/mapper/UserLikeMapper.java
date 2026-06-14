package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.UserLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserLikeMapper extends JpaRepository<UserLike, Long> {
    Optional<UserLike> findByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);

    List<UserLike> findByUserIdAndTargetTypeAndTargetIdIn(Long userId, String targetType, Collection<Long> targetIds);

    long countByTargetTypeAndTargetId(String targetType, Long targetId);
}
