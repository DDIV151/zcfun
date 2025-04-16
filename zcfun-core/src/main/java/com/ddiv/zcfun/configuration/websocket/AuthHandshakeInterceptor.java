package com.ddiv.zcfun.configuration.websocket;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * WebSocket握手拦截器，用于在WebSocket连接建立前进行身份验证。
 * 通过JWT Token验证用户身份，并检查Redis中是否存在该Token以实现单设备登录。
 */
@Component
public class AuthHandshakeInterceptor implements HandshakeInterceptor {
    private final RedisTemplate<String, Object> redisTemplate;

    // JWT密钥，用于验证Token
    @Value("${jwt.key}")
    private String key;

    /**
     * 构造函数，注入RedisTemplate实例。
     *
     * @param redisTemplate RedisTemplate实例，用于操作Redis
     */
    public AuthHandshakeInterceptor(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 在WebSocket握手之前执行，用于验证用户身份。
     * 从请求的查询参数中获取JWT Token，验证其有效性，并检查Redis中是否存在该Token。
     * 如果验证成功，将用户信息存入WebSocket会话属性中。
     *
     * @param request    HTTP请求对象
     * @param response   HTTP响应对象
     * @param wsHandler  WebSocket处理器
     * @param attributes WebSocket会话属性
     * @return 如果验证成功返回true，否则抛出异常
     * @throws AuthenticationCredentialsNotFoundException 如果验证失败抛出此异常
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = null;
        String query = request.getURI().getQuery();

        // 解析查询参数，获取Token
        if (query != null) {
            Map<String, String> params = Arrays.stream(query.split("&")) // 使用&分割不同参数
                    .map(kv -> kv.split("=", 2)) // 使用=分割参数名与值
                    .collect(Collectors.toMap(kv -> kv[0], kv -> kv.length > 1 ? kv[1] : "")); // 收集参数
            token = params.get("token");
        }

        // 验证Token是否有效
        if (token != null) {
            JWT jwt = JWTUtil.parseToken(token);
            String username = (String) jwt.getPayload().getClaim("username");
            var userId = jwt.getPayload().getClaim("user_id");
            // 检查Redis中是否存在该Token
            String redisKey = "user:token:" + username;
            String storedToken = (String) redisTemplate.opsForValue().get(redisKey);
            if (storedToken != null && storedToken.equals(token)) {
                // 将用户信息存入WebSocket会话属性
                attributes.put("token", token);
                attributes.put("username", username);
                attributes.put("user_id", userId);
                return true;
            }
        }
        throw new AuthenticationCredentialsNotFoundException("无效凭证");
    }

    /**
     * 在WebSocket握手之后执行，用于处理握手后的逻辑。
     * 当前无需处理任何逻辑，因此方法体为空。
     *
     * @param request   HTTP请求对象
     * @param response  HTTP响应对象
     * @param wsHandler WebSocket处理器
     * @param exception 握手过程中可能出现的异常
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // 握手后的逻辑，当前无需处理
    }
}