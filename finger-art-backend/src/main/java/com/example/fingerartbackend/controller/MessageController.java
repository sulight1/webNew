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

@CrossOrigin(origins = "*")
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

    @GetMapping("/unread-count/{userId}")
    public Result<Integer> getUnreadCount(@PathVariable Long userId) {
        return Result.success(messageMapper.findByReceiverIdAndIsReadFalse(userId).size());
    }

    @GetMapping("/list/{userId}")
    public Result<List<Message>> getUserMessageList(@PathVariable Long userId) {
        try {
            return Result.success(messageMapper.findAllUserMessages(userId));
        } catch (Exception e) {
            return Result.error("获取消息列表失败: " + e.getMessage());
        }
    }

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
