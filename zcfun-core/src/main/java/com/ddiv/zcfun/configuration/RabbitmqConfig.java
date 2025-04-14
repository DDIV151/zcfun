package com.ddiv.zcfun.configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
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
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // 主交换机
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

    /**
     * 创建一个点对点队列，并配置死信队列相关参数。
     * 该队列用于处理私信消息，当消息在队列中超过指定的存活时间（TTL）或无法被正常消费时，
     * 消息会被转发到指定的死信交换机和路由键。
     *
     * @return Queue 返回一个持久化的、非排他的、非自动删除的队列实例。
     * 队列名称为 "im.private.queue"，并配置了以下参数：
     * - x-dead-letter-exchange: 死信交换机名称为 "im.dlx.exchange"，
     * 用于接收无法被正常消费的消息。
     * - x-dead-letter-routing-key: 死信路由键为 "dlx.private"，
     * 用于指定死信消息的路由规则。
     * - x-message-ttl: 消息的存活时间为 86400000 毫秒（即 24 小时），
     * 超过该时间的消息将被视为死信。
     */
    @Bean
    public Queue privateQueue() {
        // 配置队列参数，包括死信交换机和路由键，以及消息的存活时间
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", "im.dlx.exchange");
        args.put("x-dead-letter-routing-key", "dlx.private");
        args.put("x-message-ttl", 86400000);

        // 创建并返回一个持久化的、非排他的、非自动删除的队列实例
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

    @Bean
    public Queue mysqlQueue() {
        return new Queue("mysql.persist.queue", true);
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
