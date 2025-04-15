package com.ddiv.zcfun.configuration.websocket;

import com.ddiv.zcfun.domain.po.im.message.MessagePO;
import com.ddiv.zcfun.domain.po.im.message.MessageType;
import com.ddiv.zcfun.exception.UserOfflineException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketServerHandler implements WebSocketHandler {

    // 在线用户列表，用于存储当前在线用户的会话信息
    private static final ConcurrentHashMap<String, WebSocketSession> onlineUsers = new ConcurrentHashMap<>();
    // 用于记录用户最后活跃时间的映射，用于检测用户是否在线（心跳机制）
    private static final ConcurrentHashMap<String, Long> lastActiveTimes = new ConcurrentHashMap<>();
    private static final long HEARTBEAT_TIMEOUT = 60000; // 60秒超时
    private static final long HEARTBEAT_INTERVAL = 30000; // 30秒检测一次

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    public WebSocketServerHandler(RedisTemplate<String, Object> redisTemplate, RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 当WebSocket连接成功建立后，该方法会被调用。
     * 将新连接的用户的会话信息存储到在线用户列表中，并将用户ID添加到Redis的在线用户集合中。
     *
     * @param session 表示当前建立的WebSocket会话，包含会话的属性和相关信息。
     * @throws Exception 如果在处理过程中发生任何异常，将抛出该异常。
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从会话属性中获取用户ID
        var userId = session.getAttributes().get("user_id");

        // 将用户ID和会话信息存储到在线用户映射中
        onlineUsers.put(userId.toString(), session);
        // 记录用户最后活跃时间（心跳处理）
        lastActiveTimes.put(userId.toString(), System.currentTimeMillis());
        // 将用户ID添加到Redis的在线用户集合中
        redisTemplate.opsForSet().add("online_users", userId);
    }

    /**
     * 定时任务，用于检测用户是否在线（心跳机制）。
     * 遍历在线用户列表，获取每个用户的最后活跃时间。
     * 如果用户最后活跃时间超过60秒（心跳超时时间），则认为用户已经离线，关闭WebSocket会话，并清理用户信息。
     *
     * @throws Exception 如果在处理过程中发生任何异常，将抛出该异常。
     */
    @Scheduled(fixedRate = HEARTBEAT_INTERVAL)
    public void checkHeartbeat() {
        long currentTime = System.currentTimeMillis();

        new ArrayList<>(lastActiveTimes.keySet()).forEach(userId -> {
            Long lastActive = lastActiveTimes.get(userId);
            if (lastActive == null || currentTime - lastActive > HEARTBEAT_TIMEOUT) {
                WebSocketSession session = onlineUsers.get(userId);
                if (session != null && session.isOpen()) {
                    try {
                        session.close(CloseStatus.SESSION_NOT_RELIABLE);
                    } catch (IOException e) {
                        // 关闭异常处理
                    }
                }
                cleanupUser(userId);
            }
        });
    }

    /**
     * 处理WebSocket消息的方法。
     * 该方法接收WebSocket会话和消息，解析消息内容，设置发送者ID和发送时间，
     * 并根据消息类型选择路由键，将消息发送到RabbitMQ主交换机。
     *
     * @param session WebSocket会话对象，包含会话属性和发送消息的方法。
     * @param message WebSocket消息对象，包含消息的payload。
     * @throws Exception 如果消息处理过程中发生异常，则抛出异常。
     */
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        try {
            // 将消息的payload解析为MessagePO对象
            MessagePO messagePO = objectMapper.readValue(
                    message.getPayload().toString(),
                    MessagePO.class
            );

            // 心跳消息特殊处理
            /*
             服务器发送心跳消息：
             {"type":"HEARTBEAT_ACK"}
             客户端收到心跳消息而发送：
             {"msg_type":"HEARTBEAT"}
            */
            if (messagePO.getMsgType() == MessageType.HEARTBEAT) {
                String userId = session.getAttributes().get("user_id").toString();
                lastActiveTimes.put(userId, System.currentTimeMillis());
                session.sendMessage(new TextMessage("{\"type\":\"HEARTBEAT_ACK\"}"));
                return;
            }

            // 从WebSocket会话属性中获取发送者ID，并设置到MessagePO对象中
            Object userIdObj = session.getAttributes().get("user_id");
            if (userIdObj == null) {
                session.sendMessage(new TextMessage("用户ID缺失"));
                return;
            }
            long senderId = Long.parseLong(userIdObj.toString());
            messagePO.setSenderId(senderId);

            // 设置消息的发送时间为当前时间
            messagePO.setSendTime(LocalDateTime.now());

            // 根据消息类型选择路由键
            MessageType msgType = messagePO.getMsgType();
            if (msgType == null) {
                session.sendMessage(new TextMessage("消息类型缺失"));
                return;
            }
            String routingKey = msgType == MessageType.PRIVATE ?
                    "private." + messagePO.getRecipientId() :
                    "group." + messagePO.getRecipientId();

            // 将消息发送到RabbitMQ主交换机，使用指定的路由键
            rabbitTemplate.convertAndSend(
                    "im.main.exchange",
                    routingKey,
                    messagePO
            );

        } catch (JsonProcessingException e) {
            session.sendMessage(new TextMessage("消息格式错误"));
        } catch (NumberFormatException e) {
            session.sendMessage(new TextMessage("用户ID格式错误"));
        } catch (Exception e) {
            // 其他异常处理
            session.sendMessage(new TextMessage("消息处理失败"));
        }
    }


    private void cleanupUser(String userId) {
        onlineUsers.remove(userId);
        lastActiveTimes.remove(userId);
        redisTemplate.opsForSet().remove("online_users", userId);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        Long userId = (Long) session.getAttributes().get("user_id");
        if (userId == null) {
            return;
        }
        // 更新在线状态（标记为离线）
        try {
            // 从在线用户集合中移除
            cleanupUser(userId.toString());
        } catch (Exception e) {
            return;
        }
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        cleanupUser(session.getAttributes().get("user_id").toString());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }


    public void sendMessageToUser(String userId, String message) throws Exception {
        WebSocketSession session = onlineUsers.get(userId);
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(message));
        } else {
            redisTemplate.opsForSet().remove("online_users", userId);
            throw new UserOfflineException("用户不在线");
        }
    }
}
