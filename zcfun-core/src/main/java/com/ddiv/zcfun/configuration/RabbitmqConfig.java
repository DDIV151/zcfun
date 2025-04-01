package com.ddiv.zcfun.configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


@Configuration
@EnableRabbit
public class RabbitmqConfig {

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setConnectionFactory(connectionFactory);
        return rabbitTemplate;
    }

    // 主交换机改为主题类型
    @Bean
    public TopicExchange mainExchange() {
        return new TopicExchange("im.main.exchange", true, false);
    }

    // 群组主题交换机
    @Bean
    public TopicExchange groupExchange() {
        return new TopicExchange("im.group.exchange", true, false);
    }

    // 死信交换机
    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange("im.dlx.exchange", true, false);
    }

    // 点对点队列（带死信配置）
    @Bean
    public Queue privateQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", "im.dlx.exchange");
        args.put("x-dead-letter-routing-key", "dlx.private");
        args.put("x-message-ttl", 86400000);
        return new Queue("im.private.queue", true, false, false, args);
    }

    // 群组队列
    @Bean
    public Queue groupQueue() {
        return new Queue("im.group.queue", true);
    }

    // 离线队列（按用户区分）
    @Bean
    public Queue offlineQueue() {
        return new Queue("im.offline.queue", true);
    }

    // 死信队列
    @Bean
    public Queue dlxQueue() {
        return new Queue("im.dlx.queue", true);
    }

    // 绑定关系
    @Bean
    public Declarables bindings() {
        return new Declarables(
                // 私聊消息路由到所有用户队列
                BindingBuilder.bind(privateQueue()).to(mainExchange()).with("private.*"),
                // 群组消息路由
                BindingBuilder.bind(groupQueue()).to(groupExchange()).with("group.#"),
                // 离线消息按用户路由
                BindingBuilder.bind(offlineQueue()).to(mainExchange()).with("offline.*"),
                // 死信队列绑定
                BindingBuilder.bind(dlxQueue()).to(dlxExchange()).with("dlx.private")
        );
    }
}
