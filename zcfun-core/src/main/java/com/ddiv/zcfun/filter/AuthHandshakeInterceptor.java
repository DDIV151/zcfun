package com.ddiv.zcfun.filter;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class AuthHandshakeInterceptor implements HandshakeInterceptor {
    //握手拦截器，用来进行ws连接建立前的验证
    private final String key;

    public AuthHandshakeInterceptor(@Value("${jwt.key}") String key) {
        this.key = key;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String token = request.getHeaders().getFirst("Authorization");
        if (token != null && JWTUtil.verify(token, key.getBytes())) {
            attributes.put("token", token);
            JWT jwt = JWTUtil.parseToken(token);
            attributes.put("username", jwt.getPayload().getClaim("username"));
            attributes.put("userId", jwt.getPayload().getClaim("user_id"));
            return true;
        } else {
            throw new AuthenticationCredentialsNotFoundException("无效凭证");
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        return;
    }
}
