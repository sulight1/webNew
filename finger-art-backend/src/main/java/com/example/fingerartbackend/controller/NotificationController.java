package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.entity.PlatformNotification;
import com.example.fingerartbackend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 平台通知控制器。
 * 管理用户系统通知的查询、已读标记与批量清理，对应消息通知模块。
 */
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 查询用户的全部平台通知。
     *
     * @param userId 用户 ID
     * @return 通知列表
     */
    @GetMapping
    public Result<List<PlatformNotification>> list(@RequestParam Long userId) {
        return Result.success(notificationService.getUserNotifications(userId));
    }

    /**
     * 查询用户未读通知数量。
     *
     * @param userId 用户 ID
     * @return 未读通知条数
     */
    @GetMapping("/unread-count")
    public Result<Long> unreadCount(@RequestParam Long userId) {
        return Result.success(notificationService.getUnreadCount(userId));
    }

    /**
     * 将单条通知标记为已读。
     *
     * @param id   通知 ID
     * @param body 含 userId 的请求体，用于权限校验
     * @return 操作成功提示
     */
    @PatchMapping("/{id}/read")
    public Result<String> markRead(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        try {
            notificationService.markRead(id, body.get("userId"));
            return Result.success("已读");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 将用户全部通知标记为已读。
     *
     * @param userId 用户 ID
     * @return 操作成功提示
     */
    @PatchMapping("/read-all")
    public Result<String> markAllRead(@RequestParam Long userId) {
        notificationService.markAllRead(userId);
        return Result.success("全部已读");
    }

    /**
     * 删除用户全部已读通知。
     *
     * @param userId 用户 ID
     * @return 删除条数及提示信息
     */
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
