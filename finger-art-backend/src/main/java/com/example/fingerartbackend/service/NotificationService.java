package com.example.fingerartbackend.service;

import com.example.fingerartbackend.entity.PlatformNotification;

import java.util.List;

public interface NotificationService {
    PlatformNotification notify(Long userId, String type, String title, String content, String linkUrl);

    List<PlatformNotification> getUserNotifications(Long userId);

    long getUnreadCount(Long userId);

    void markRead(Long notificationId, Long userId);

    void markAllRead(Long userId);

    int deleteAllRead(Long userId);
}
