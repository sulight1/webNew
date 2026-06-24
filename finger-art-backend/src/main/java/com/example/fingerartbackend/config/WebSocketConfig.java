package com.example.fingerartbackend.config;

import com.example.fingerartbackend.realtime.RealtimeWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 配置。
 * <p>
 * 注册实时推送 WebSocket 端点 {@code /ws/realtime}，
 * 供客户端建立长连接接收站内消息、订单状态等实时通知。
 * </p>
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private RealtimeWebSocketHandler realtimeWebSocketHandler;

    /**
     * 用户注册。
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(realtimeWebSocketHandler, "/ws/realtime")
                .setAllowedOrigins("*");
    }
}
