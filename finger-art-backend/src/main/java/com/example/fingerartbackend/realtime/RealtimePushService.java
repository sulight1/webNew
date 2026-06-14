package com.example.fingerartbackend.realtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RealtimePushService {

    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void register(Long userId, WebSocketSession session) {
        if (userId != null) {
            sessions.put(userId, session);
        }
    }

    public void unregister(Long userId) {
        if (userId != null) {
            sessions.remove(userId);
        }
    }

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
