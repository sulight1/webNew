package com.example.fingerartbackend.service.impl;

import com.example.fingerartbackend.entity.UserLike;
import com.example.fingerartbackend.mapper.UserLikeMapper;
import com.example.fingerartbackend.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 点赞服务实现类。
 */
@Service
public class LikeServiceImpl implements LikeService {

    @Autowired
    private UserLikeMapper userLikeMapper;

    /**
     * 切换点赞状态。
     */
    @Override
    @Transactional
    public boolean toggle(Long userId, String targetType, Long targetId) {
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        return userLikeMapper.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId)
                .map(existing -> {
                    userLikeMapper.delete(existing);
                    return false;
                })
                .orElseGet(() -> {
                    UserLike like = new UserLike();
                    like.setUserId(userId);
                    like.setTargetType(targetType);
                    like.setTargetId(targetId);
                    userLikeMapper.save(like);
                    return true;
                });
    }

    /**
     * 判断条件是否成立。
     */
    @Override
    public boolean isLiked(Long userId, String targetType, Long targetId) {
        if (userId == null || targetId == null) {
            return false;
        }
        return userLikeMapper.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId).isPresent();
    }

    /**
     * 查询点赞信息。
     */
    @Override
    public Set<Long> getLikedTargetIds(Long userId, String targetType, Collection<Long> targetIds) {
        if (userId == null || targetIds == null || targetIds.isEmpty()) {
            return Collections.emptySet();
        }
        return userLikeMapper.findByUserIdAndTargetTypeAndTargetIdIn(userId, targetType, targetIds).stream()
                .map(UserLike::getTargetId)
                .collect(Collectors.toSet());
    }
}
