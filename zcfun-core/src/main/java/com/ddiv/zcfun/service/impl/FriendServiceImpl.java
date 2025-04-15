package com.ddiv.zcfun.service.impl;

import com.ddiv.zcfun.mapper.FriendMapper;
import com.ddiv.zcfun.service.FriendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class FriendServiceImpl implements FriendService {
    private static final Logger log = LoggerFactory.getLogger(FriendServiceImpl.class);
    private final FriendMapper friendMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public FriendServiceImpl(FriendMapper friendMapper, RedisTemplate<String, Object> redisTemplate) {
        this.friendMapper = friendMapper;
        this.redisTemplate = redisTemplate;
    }

    public boolean areFriends(long userA, long userB) {
        if (userA == userB) {
            return true; // 允许发送给自己的情况
        }

        // 统一缓存键的生成方式，使用两个用户ID的较小值作为键的前缀，较大值作为键的后缀
        long smallerUserId = Math.min(userA, userB);
        long largerUserId = Math.max(userA, userB);
        String cacheKey = String.format("friendship:%d:%d", smallerUserId, largerUserId);

        try {
            // 尝试从缓存获取
            Boolean cachedResult = (Boolean) redisTemplate.opsForValue().get(cacheKey);
            if (cachedResult != null) {
                return cachedResult;
            }

            // 查询数据库
            boolean exists = friendMapper.existsFriendship(userA, userB) > 0;
            redisTemplate.opsForValue().set(cacheKey, exists, 1, TimeUnit.HOURS); // 缓存1小时
            return exists;
        } catch (Exception e) {
            log.error("Error occurred while checking friendship between users: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void evictFriendshipCache(long userA, long userB) {
        long smallerId = Math.min(userA, userB);
        long largerId = Math.max(userA, userB);
        String cacheKey = String.format("friendship:%d:%d", smallerId, largerId);
        redisTemplate.delete(cacheKey);
    }

}
