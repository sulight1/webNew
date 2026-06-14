package com.example.fingerartbackend.service;

import java.util.Collection;
import java.util.Set;

public interface LikeService {
    /** 切换点赞状态，返回操作后是否已点赞 */
    boolean toggle(Long userId, String targetType, Long targetId);

    boolean isLiked(Long userId, String targetType, Long targetId);

    Set<Long> getLikedTargetIds(Long userId, String targetType, Collection<Long> targetIds);
}
