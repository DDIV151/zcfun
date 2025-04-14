package com.ddiv.zcfun.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

@Configuration
public class RedisConfig {

    // 自定义ObjectMapper，注册JavaTimeModule并应用配置

    /**
     * 配置并返回一个自定义的 {@link ObjectMapper} Bean。
     * 该 ObjectMapper 用于处理 JSON 序列化和反序列化，特别针对日期和时间格式进行了定制。
     *
     * @return 配置好的 {@link ObjectMapper} 实例，用于处理 JSON 数据。
     */
    @Bean
    public ObjectMapper objectMapper() {
        // 创建一个新的 ObjectMapper 实例
        ObjectMapper mapper = new ObjectMapper();

        // 注册 JavaTimeModule 模块，以支持 Java 8 日期时间 API 的序列化和反序列化（LocalDateTime）
        mapper.registerModule(new JavaTimeModule());

        // 禁用将日期序列化为时间戳的默认行为，改为使用更易读的字符串格式
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 设置日期格式为 "yyyy-MM-dd HH:mm:ss"，确保日期和时间以统一的格式进行序列化和反序列化
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        // 设置时区为 "Asia/Shanghai"，确保日期和时间在序列化和反序列化时使用正确的时区
        mapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

        return mapper;
    }

    /**
     * 配置并返回一个自定义的RedisTemplate实例，使用指定的Redis连接工厂和ObjectMapper。
     * 该RedisTemplate使用String序列化器来序列化键和哈希键，使用Jackson2JsonRedisSerializer
     * 来序列化值和哈希值，其中Jackson2JsonRedisSerializer使用传入的自定义ObjectMapper。
     *
     * @param factory Redis连接工厂，用于创建Redis连接。
     * @param objectMapper 自定义的ObjectMapper，用于配置Jackson2JsonRedisSerializer。
     * @return 配置好的RedisTemplate实例，用于操作Redis数据库。
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory factory,
            ObjectMapper objectMapper) {

        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);

        // 配置键和哈希键的序列化器为StringRedisSerializer
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);

        // 配置值和哈希值的序列化器为Jackson2JsonRedisSerializer，使用自定义的ObjectMapper
        Jackson2JsonRedisSerializer<Object> jsonSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

        redisTemplate.setValueSerializer(jsonSerializer);
        redisTemplate.setHashValueSerializer(jsonSerializer);

        // 确保所有属性设置完成后进行初始化
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
