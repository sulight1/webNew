package com.example.fingerartbackend.realtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实时消息推送服务。
 * <p>
 * 维护用户 ID 与 WebSocket 会话的映射，向在线用户推送通知、订单状态变更等实时事件。
 * 连接异常时静默忽略，客户端可通过轮询兜底。
 * </p>
 */
@Service
public class RealtimePushService {

    /** 用户 ID → WebSocket 会话 */
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 注册用户 WebSocket 会话。
     *
     * @param userId  用户 ID
     * @param session WebSocket 会话
     */
    public void register(Long userId, WebSocketSession session) {
        if (userId != null) {
            sessions.put(userId, session);
        }
    }

    /**
     * 注销用户 WebSocket 会话（连接关闭时调用）。
     *
     * @param userId 用户 ID
     */
    public void unregister(Long userId) {
        if (userId != null) {
            sessions.remove(userId);
        }
    }

    /**
     * 向指定在线用户推送实时消息。
     *
     * @param userId  目标用户 ID
     * @param type    消息类型（如 NOTIFICATION、ORDER_UPDATE）
     * @param payload 消息载荷
     */
    public void pushToUser(Long userId, String type, Object payload) {
        if (userId == null) return;
        WebSocketSession session = sessions.get(userId);
        if (session == null || !session.isOpen()) return;
        try {
            Map<String, Object> envelope = Map.of("type", type, "payload", payload);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(envelope)));
        } catch (Exception ignored) {
            // 连接异常时忽略，客户端可轮询兜底
        }
    }
}
