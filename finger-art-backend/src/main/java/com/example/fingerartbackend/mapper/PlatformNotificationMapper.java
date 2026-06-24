package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.PlatformNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 平台通知实体 {@link PlatformNotification} 的数据访问层。
 * <p>
 * 负责系统消息、订单提醒等站内通知的持久化及未读统计。
 * </p>
 */
@Repository
public interface PlatformNotificationMapper extends JpaRepository<PlatformNotification, Long> {

    /** 查询某用户的全部通知，按创建时间降序 */
    List<PlatformNotification> findByUserIdOrderByCreatedAtDesc(Long userId);

    /** 统计某用户的未读通知数量 */
    long countByUserIdAndIsReadFalse(Long userId);

    /** 清理某用户已读通知 */
    void deleteByUserIdAndIsReadTrue(Long userId);
}
