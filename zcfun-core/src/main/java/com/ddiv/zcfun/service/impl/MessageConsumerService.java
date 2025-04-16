package com.ddiv.zcfun.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.ddiv.zcfun.configuration.websocket.WebSocketServerHandler;
import com.ddiv.zcfun.domain.po.im.message.MessagePO;
import com.ddiv.zcfun.domain.po.im.message.OfflineMessagePO;
import com.ddiv.zcfun.exception.UserOfflineException;
import com.ddiv.zcfun.mapper.MessageMapper;
import com.ddiv.zcfun.service.FriendService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MessageConsumerService {

    private final WebSocketServerHandler webSocketHandler;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;
    private final Snowflake snowflake;
    private final MessageMapper messageMapper;
    private final FriendService friendService;
    private final GroupServiceImpl groupService;

    public MessageConsumerService(WebSocketServerHandler webSocketHandler, RedisTemplate<String, Object> redisTemplate,
                                  ObjectMapper objectMapper, RabbitTemplate rabbitTemplate,
                                  Snowflake snowflake, MessageMapper messageMapper,
                                  FriendService friendService, GroupServiceImpl groupService) {
        this.webSocketHandler = webSocketHandler;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.rabbitTemplate = rabbitTemplate;
        this.snowflake = snowflake;
        this.messageMapper = messageMapper;
        this.friendService = friendService;
        this.groupService = groupService;
    }

    private static final String OFFLINE_ROUTING_KEY = "offline.";

    @Value("${im.keys.online-user-set}")
    private String ONLINE_USER_SET_KEY;


    /**
     * 处理私聊消息的监听器方法。
     * 该方法监听名为 "im.private.queue" 的 RabbitMQ 队列，接收到消息后根据接收者是否在线决定消息的处理方式。
     * 如果接收者在线，则通过 WebSocket 发送消息；如果接收者离线，则将消息重新发送到离线消息队列中。
     *
     * @param messagePO 接收到的私聊消息对象，包含消息内容和接收者信息。
     */
    @RabbitListener(queues = "im.private.queue", concurrency = "5-10")
    public void processPrivateMessage(MessagePO messagePO) {
        Long senderId = messagePO.getSenderId();
        Long recipientId = messagePO.getRecipientId();
        // 检查发送者和接收者是否为好友关系 和 是否被屏蔽
        if (!friendService.areFriends(senderId, recipientId) && !friendService.isBlocked(senderId, recipientId)) {
            log.warn("User {} is not {}'s friend or he/she is blocked", recipientId, senderId);
            return;
        }
        messagePO.setMsgId(snowflake.nextId());
        try {
            // 检查接收者是否在线
            if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(ONLINE_USER_SET_KEY, recipientId.toString()))) {
                try {
                    // 如果接收者在线，通过 WebSocket 发送消息
                    webSocketHandler.sendMessageToUser(String.valueOf(recipientId), objectMapper.writeValueAsString(messagePO));
                    // 发送成功保存到数据库
                    asyncSaveToMySQL(messagePO);
                } catch (UserOfflineException e) {
                    rabbitTemplate.convertAndSend("im.main.exchange", OFFLINE_ROUTING_KEY + recipientId, messagePO);
                    log.error("Failed to send message to user {}", recipientId, e);
                } catch (JsonProcessingException e) {
                    log.error("Failed to serialize message", e);
                    throw new RuntimeException("Failed to serialize message", e);
                }
            } else {
                // 如果接收者离线，直接将消息发送到离线消息队列
                rabbitTemplate.convertAndSend("im.main.exchange", OFFLINE_ROUTING_KEY + recipientId, messagePO);
            }
        } catch (Exception e) {
            // 如果处理过程中发生异常，抛出运行时异常
            log.error("Failed to process private message", e);
            throw new RuntimeException("Failed to process private message", e);
        }
    }

    /**
     * 处理群组消息的监听器方法。该方法从RabbitMQ的"im.group.queue"队列中消费消息，并处理群组消息的分发。
     * 消息会被异步保存到MySQL数据库，并根据群组成员的在线状态进行分发。在线成员通过WebSocket接收消息，
     * 离线成员的消息则会被存入离线队列。
     *
     * @param messagePO 包含消息内容的对象，包括发送者、接收者等信息。
     */
    @RabbitListener(queues = "im.group.queue", concurrency = "5-10")
    public void processGroupMessage(MessagePO messagePO) {
        // 为消息生成唯一的ID
        messagePO.setMsgId(snowflake.nextId());
        try {
            // 异步将消息保存到MySQL数据库
            asyncSaveToMySQL(messagePO);
            Long groupId = messagePO.getRecipientId();
            Long senderId = messagePO.getSenderId();

            // 获取所有群成员（包括在线和离线）
            Set<Long> allMembers = groupService.getAllGroupMembers(groupId);

            // 获取当前在线的群成员
            Set<Long> onlineMembers = groupService.getGroupOnlineMembers(groupId);

            // 排除发送者，避免发送者收到自己发送的消息
            onlineMembers = onlineMembers.stream()
                    .filter(memberId -> !memberId.equals(senderId))
                    .collect(Collectors.toSet());

            // 计算离线成员：所有成员 - 在线成员 - 发送者
            Set<Long> finalOnlineMembers = onlineMembers;
            Set<Long> offlineMembers = allMembers.stream()
                    .filter(memberId -> !finalOnlineMembers.contains(memberId) && !memberId.equals(senderId))
                    .collect(Collectors.toSet());

            // 处理在线成员：尝试通过WebSocket推送消息，若失败则转存到离线队列
            onlineMembers.parallelStream().forEach(memberId -> {
                try {
                    webSocketHandler.sendMessageToUser(
                            String.valueOf(memberId),
                            objectMapper.writeValueAsString(messagePO)
                    );
                } catch (UserOfflineException e) {
                    rabbitTemplate.convertAndSend("im.main.exchange", OFFLINE_ROUTING_KEY + memberId, messagePO);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Failed to serialize message", e);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            // 处理离线成员：直接将消息存入离线队列
            offlineMembers.parallelStream().forEach(memberId -> {
                rabbitTemplate.convertAndSend("im.main.exchange", OFFLINE_ROUTING_KEY + memberId, messagePO);
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 独立的消费者服务监听 mysql.persist.queue
    @RabbitListener(queues = "mysql.persist.queue", concurrency = "5-10")
    public void saveToMySQL(MessagePO messagePO) {
        messageMapper.save(messagePO);
    }

    @RabbitListener(queues = "im.offline.queue", concurrency = "5-10")
    public void handleDeadLetterMessage(MessagePO message) {
        try {
            messageMapper.saveOfflineMessage(makeOfflineMessagePO(message));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private OfflineMessagePO makeOfflineMessagePO(MessagePO messagePO) throws JsonProcessingException {
        OfflineMessagePO message = new OfflineMessagePO();
        message.setId(messagePO.getMsgId());
        message.setRecipientId(messagePO.getRecipientId());
        message.setContent(objectMapper.writeValueAsString(messagePO));
        return message;
    }

    private void asyncSaveToMySQL(MessagePO messagePO) {
        if (messagePO != null)
            rabbitTemplate.convertAndSend("mysql.persist.queue", messagePO);
    }


}