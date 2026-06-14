package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.entity.PlatformNotification;
import com.example.fingerartbackend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public Result<List<PlatformNotification>> list(@RequestParam Long userId) {
        return Result.success(notificationService.getUserNotifications(userId));
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount(@RequestParam Long userId) {
        return Result.success(notificationService.getUnreadCount(userId));
    }

    @PatchMapping("/{id}/read")
    public Result<String> markRead(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        try {
            notificationService.markRead(id, body.get("userId"));
            return Result.success("已读");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PatchMapping("/read-all")
    public Result<String> markAllRead(@RequestParam Long userId) {
        notificationService.markAllRead(userId);
        return Result.success("全部已读");
    }

    @DeleteMapping("/read")
    public Result<Map<String, Object>> deleteAllRead(@RequestParam Long userId) {
        try {
            int deleted = notificationService.deleteAllRead(userId);
            return Result.success(Map.of("deleted", deleted, "message", "已删除 " + deleted + " 条已读通知"));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
