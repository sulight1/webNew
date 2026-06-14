package com.example.fingerartbackend.service.impl;

import com.example.fingerartbackend.entity.PlatformNotification;
import com.example.fingerartbackend.mapper.PlatformNotificationMapper;
import com.example.fingerartbackend.realtime.RealtimePushService;
import com.example.fingerartbackend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private PlatformNotificationMapper notificationMapper;

    @Autowired
    private RealtimePushService pushService;

    @Override
    @Transactional
    public PlatformNotification notify(Long userId, String type, String title, String content, String linkUrl) {
        PlatformNotification n = new PlatformNotification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setLinkUrl(linkUrl);
        PlatformNotification saved = notificationMapper.save(n);
        pushService.pushToUser(userId, "NOTIFICATION", saved);
        return saved;
    }

    @Override
    public List<PlatformNotification> getUserNotifications(Long userId) {
        return notificationMapper.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationMapper.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public void markRead(Long notificationId, Long userId) {
        PlatformNotification n = notificationMapper.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("通知不存在"));
        if (!userId.equals(n.getUserId())) {
            throw new RuntimeException("无权操作该通知");
        }
        n.setIsRead(true);
        notificationMapper.save(n);
    }

    @Override
    @Transactional
    public void markAllRead(Long userId) {
        notificationMapper.findByUserIdOrderByCreatedAtDesc(userId).forEach(n -> {
            n.setIsRead(true);
            notificationMapper.save(n);
        });
    }

    @Override
    @Transactional
    public int deleteAllRead(Long userId) {
        long before = notificationMapper.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(n -> Boolean.TRUE.equals(n.getIsRead()))
                .count();
        notificationMapper.deleteByUserIdAndIsReadTrue(userId);
        return (int) before;
    }
}
