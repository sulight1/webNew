package com.example.fingerartbackend.realtime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.Map;

/**
 * 实时 WebSocket 连接处理器。
 * <p>
 * 处理客户端与 {@code /ws/realtime} 端点的连接生命周期：
 * 连接建立时从 URL 参数提取 userId 并注册会话，
 * 连接关闭时注销会话，支持客户端 ping 心跳保活。
 * </p>
 */
@Component
public class RealtimeWebSocketHandler extends TextWebSocketHandler {

    /** 会话属性中存储用户 ID 的键名 */
    private static final String USER_ID_ATTR = "userId";

    @Autowired
    private RealtimePushService pushService;

    /**
     * 连接建立：解析 userId 并注册到推送服务。
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = extractUserId(session);
        if (userId != null) {
            session.getAttributes().put(USER_ID_ATTR, userId);
            pushService.register(userId, session);
        }
    }

    /**
     * 连接关闭：从推送服务注销会话。
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Object userId = session.getAttributes().get(USER_ID_ATTR);
        if (userId instanceof Long id) {
            pushService.unregister(id);
        }
    }

    /**
     * 处理客户端文本消息，响应 ping 心跳。
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 客户端心跳
        if ("ping".equalsIgnoreCase(message.getPayload())) {
            try {
                session.sendMessage(new TextMessage("{\"type\":\"PONG\"}"));
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 从 WebSocket 连接 URL 的 query 参数中提取 userId。
     * 期望格式：{@code /ws/realtime?userId=123}
     *
     * @param session WebSocket 会话
     * @return 用户 ID，解析失败返回 null
     */
    private Long extractUserId(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null || uri.getQuery() == null) return null;
        for (String part : uri.getQuery().split("&")) {
            if (part.startsWith("userId=")) {
                try {
                    return Long.parseLong(part.substring(7));
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }
}
