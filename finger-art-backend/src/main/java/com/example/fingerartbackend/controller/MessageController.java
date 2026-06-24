package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.entity.Message;
import com.example.fingerartbackend.mapper.MessageMapper;
import com.example.fingerartbackend.realtime.RealtimePushService;
import com.example.fingerartbackend.service.NotificationService;
import com.example.fingerartbackend.service.SensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 私信消息控制器。
 * 处理用户间私信的发送、聊天记录、未读数及消息列表，对应即时通讯模块。
 */
@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private RealtimePushService pushService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SensitiveWordService sensitiveWordService;

    /**
     * 发送私信消息，并进行敏感词校验与实时推送。
     *
     * @param message 消息实体（发送者、接收者、内容等）
     * @return 保存后的消息记录
     */
    @PostMapping
    public Result<Message> sendMessage(@RequestBody Message message) {
        try {
            if (message.getReceiverId() == null) {
                return Result.error("接收者ID不能为空");
            }
            sensitiveWordService.validateText(message.getContent(), "消息内容");
            Message saved = messageMapper.save(message);
            pushService.pushToUser(saved.getReceiverId(), "CHAT_MESSAGE", saved);
            String preview = saved.getContent();
            if (preview != null && preview.length() > 40) {
                preview = preview.substring(0, 40) + "...";
            }
            notificationService.notify(
                    saved.getReceiverId(),
                    "MESSAGE",
                    "新私信",
                    saved.getSenderName() + "：" + preview,
                    "/artisan-dashboard?menu=messages");
            return Result.success(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("发送消息失败: " + e.getMessage());
        }
    }

    /**
     * 获取两用户间的聊天历史，并将接收方未读消息标记为已读。
     *
     * @param userId1 用户 A 的 ID
     * @param userId2 用户 B 的 ID
     * @return 按时间排序的消息列表
     */
    @GetMapping("/chat")
    public Result<List<Message>> getChatHistory(@RequestParam Long userId1, @RequestParam Long userId2) {
        try {
            List<Message> history = messageMapper.findChatHistory(userId1, userId2);
            history.forEach(m -> {
                if (m.getReceiverId().equals(userId1)) {
                    m.setRead(true);
                }
            });
            messageMapper.saveAll(history);
            return Result.success(history);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取聊天记录失败: " + e.getMessage());
        }
    }

    /**
     * 查询用户未读私信数量。
     *
     * @param userId 用户 ID
     * @return 未读消息条数
     */
    @GetMapping("/unread-count/{userId}")
    public Result<Integer> getUnreadCount(@PathVariable Long userId) {
        return Result.success(messageMapper.findByReceiverIdAndIsReadFalse(userId).size());
    }

    /**
     * 获取用户的私信会话列表（含最近消息摘要）。
     *
     * @param userId 用户 ID
     * @return 消息列表
     */
    @GetMapping("/list/{userId}")
    public Result<List<Message>> getUserMessageList(@PathVariable Long userId) {
        try {
            return Result.success(messageMapper.findAllUserMessages(userId));
        } catch (Exception e) {
            return Result.error("获取消息列表失败: " + e.getMessage());
        }
    }

    /**
     * 删除指定私信消息。
     *
     * @param id 消息 ID
     * @return 删除成功提示
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteMessage(@PathVariable Long id) {
        try {
            messageMapper.deleteById(id);
            return Result.success("消息已删除");
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }
}
