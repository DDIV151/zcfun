package com.ddiv.zcfun.configuration.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

@EnableWebSocket
@Configuration
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketHandler webSocketServerHandler;
    private final HandshakeInterceptor authHandshakeInterceptor;

    public WebSocketConfig(AuthHandshakeInterceptor authHandshakeInterceptor, WebSocketServerHandler webSocketServerHandler) {
        this.authHandshakeInterceptor = authHandshakeInterceptor;
        this.webSocketServerHandler = webSocketServerHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry//消息(连接)处理器
                .addHandler(webSocketServerHandler, "/ws/im", "/ws/im/send")
                //握手前确认
                .addInterceptors(authHandshakeInterceptor)
                //允许跨域
                .setAllowedOrigins("*");

    }
}