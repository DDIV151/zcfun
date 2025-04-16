package com.ddiv.zcfun.service.impl;

import com.ddiv.zcfun.domain.po.BlockPO;
import com.ddiv.zcfun.domain.po.im.FriendPO;
import com.ddiv.zcfun.mapper.FriendMapper;
import com.ddiv.zcfun.service.FriendService;
import com.ddiv.zcfun.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 好友服务实现类，实际实现的是单向关注关系。
 * 好友关系是单向的，即用户A关注用户B，用户B不需要再次关注用户A。
 * 即使关注，也可以被一方屏蔽（单向），关注关系和屏蔽关系是独立的，用户无法知道是否被对方屏蔽。
 */
@Service("friendService")
public class FriendServiceImpl implements FriendService {
    private static final Logger log = LoggerFactory.getLogger(FriendServiceImpl.class);
    private final FriendMapper friendMapper; // 好友关系数据访问对象
    private final RedisTemplate<String, Object> redisTemplate; // Redis缓存操作对象
    private final UserService userService; // 用户服务对象

    private static final String FRIENDSHIP_CACHE_KEY_FORMAT = "friendship:%d:%d"; // 好友关系缓存键格式
    private static final String BLOCK_CACHE_KEY_FORMAT = "block:%d:%d"; // 屏蔽关系缓存键格式

    /**
     * 构造函数，注入依赖。
     *
     * @param friendMapper    好友关系数据访问对象
     * @param redisTemplate   Redis缓存操作对象
     * @param userService     用户服务对象
     */
    public FriendServiceImpl(FriendMapper friendMapper, RedisTemplate<String, Object> redisTemplate, UserService userService) {
        this.friendMapper = friendMapper;
        this.redisTemplate = redisTemplate;
        this.userService = userService;
    }

    /**
     * 检查两个用户是否为好友（单向关注即可）。
     *
     * @param userA 用户A的ID
     * @param userB 用户B的ID
     * @return 如果用户A关注了用户B，则返回true，否则返回false
     */
    @Override
    public boolean areFriends(long userA, long userB) {
        if (userA == userB) {
            return true; // 允许发送给自己的情况
        }

        // 统一缓存键的生成方式，使用两个用户ID的较小值作为键的前缀，较大值作为键的后缀
        long smallerUserId = Math.min(userA, userB);
        long largerUserId = Math.max(userA, userB);
        String cacheKey = String.format(FRIENDSHIP_CACHE_KEY_FORMAT, smallerUserId, largerUserId);

        try {
            // 尝试从缓存获取
            Boolean cachedResult = (Boolean) redisTemplate.opsForValue().get(cacheKey);
            if (cachedResult != null) {
                return cachedResult;
            }

            // 查询数据库
            boolean exists = friendMapper.existsFriendship(smallerUserId, largerUserId) > 0;
            updateFriendshipCache(smallerUserId, largerUserId, exists);
            return exists;
        } catch (Exception e) {
            log.error("Error occurred while checking friendship between users: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查是否屏蔽。
     *
     * @param userId    用户ID
     * @param blockedId 被屏蔽用户ID
     * @return 如果用户屏蔽了被屏蔽用户，则返回true，否则返回false
     */
    @Override
    public boolean isBlocked(long userId, long blockedId) {
        if (userId == blockedId) {
            return false; // 不能屏蔽自己
        }
        checkUserExist(userId, blockedId);
        String cacheKey = getBlockCacheKey(userId, blockedId);
        try {
            // 尝试从缓存获取
            Boolean cachedResult = (Boolean) redisTemplate.opsForValue().get(cacheKey);
            if (cachedResult != null) {
                return cachedResult;
            }
            // 查询数据库
            boolean exists = friendMapper.existsBlock(userId, blockedId) > 0;
            evictBlockCache(userId, blockedId);
            return exists;
        } catch (Exception e) {
            log.error("Error occurred while checking block status: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 添加屏蔽关系。
     *
     * @param userId    用户ID
     * @param blockedId 被屏蔽用户ID
     * @throws IllegalArgumentException 如果用户尝试屏蔽自己
     */
    @Override
    public void blockUser(long userId, long blockedId) {
        if (userId == blockedId) {
            throw new IllegalArgumentException("Cannot block yourself");
        }
        checkUserExist(userId, blockedId);
        // 如果已经屏蔽，则直接返回
        if (isBlocked(userId, blockedId))
            return;
        // 如果已经存在，则更新
        if (friendMapper.updateBlock(userId, blockedId) > 0) {
            evictBlockCache(userId, blockedId);
            return;
        }
        // 不存在，则插入
        BlockPO blockPO = new BlockPO();
        blockPO.setUserId(userId);
        blockPO.setBlockedId(blockedId);
        friendMapper.insertBlock(blockPO);
        updateBlockCache(userId, blockedId, true);
    }

    /**
     * 取消屏蔽关系。
     *
     * @param userId    用户ID
     * @param blockedId 被屏蔽用户ID
     */
    @Override
    public void unblockUser(long userId, long blockedId) {
        if (isBlocked(userId, blockedId)) {
            friendMapper.updateBlock(userId, blockedId);
            try {
                updateBlockCache(userId, blockedId, false);
            } catch (Exception e) {
                evictBlockCache(userId, blockedId);
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 清除屏蔽缓存。
     *
     * @param userId    用户ID
     * @param blockedId 被屏蔽用户ID
     */
    @Override
    public void evictBlockCache(long userId, long blockedId) {
        String cacheKey = getBlockCacheKey(userId, blockedId);
        redisTemplate.delete(cacheKey);
    }

    /**
     * 获取用户的好友列表。
     *
     * @param userId 用户ID
     * @return 用户的好友列表
     */
    @Override
    public List<FriendPO> getFriends(long userId) {
        return friendMapper.getFriends(userId);
    }

    /**
     * 添加好友关系。
     *
     * @param userId   用户ID
     * @param friendId 好友ID
     */
    @Override
    public void addFriend(long userId, long friendId) {
        checkUserExist(userId, friendId);
        if (areFriends(userId, friendId)) {
            return;
        }
        long smallerId = Math.min(userId, friendId);
        long largerId = Math.max(userId, friendId);
        friendMapper.insertFriend(new FriendPO(smallerId, largerId, null, null, 0));
        updateFriendshipCache(smallerId, largerId, true);
    }

    /**
     * 删除好友关系。
     *
     * @param userId   用户ID
     * @param friendId 好友ID
     */
    @Override
    public void deleteFriend(long userId, long friendId) {
        checkUserExist(userId, friendId);
        if (!areFriends(userId, friendId)) {
            return;
        }
        long smallerId = Math.min(userId, friendId);
        long largerId = Math.max(userId, friendId);
        friendMapper.updateFriend(smallerId, largerId);
        updateFriendshipCache(smallerId, largerId, false);
    }

    /**
     * 获取用户屏蔽的用户列表。
     *
     * @param userId 用户ID
     * @return 用户屏蔽的用户列表
     */
    @Override
    public List<FriendPO> getBlockedUsers(long userId) {
        if (userService.checkUserExists(userId)) {
            return friendMapper.getBlockedUsers(userId);
        }
        return null;
    }

    /**
     * 清除好友关系缓存。
     *
     * @param userA 用户A的ID
     * @param userB 用户B的ID
     */
    @Override
    public void evictFriendshipCache(long userA, long userB) {
        redisTemplate.delete(getFriendshipCacheKey(userA, userB));
    }

    /**
     * 检查用户是否存在。
     *
     * @param userId 用户ID数组
     * @throws IllegalArgumentException 如果用户不存在
     */
    private void checkUserExist(long... userId) {
        for (long id : userId)
            if (!userService.checkUserExists(id)) {
                throw new IllegalArgumentException("User does not exist");
            }
    }

    /**
     * 更新好友关系缓存。
     *
     * @param smallerId 较小的用户ID
     * @param largerId  较大的用户ID
     * @param isFriend  是否为好友关系
     */
    private void updateFriendshipCache(long smallerId, long largerId, boolean isFriend) {
        String cacheKey = String.format(FRIENDSHIP_CACHE_KEY_FORMAT, smallerId, largerId);
        redisTemplate.opsForValue().set(cacheKey, isFriend, 1, TimeUnit.HOURS);
    }

    /**
     * 更新屏蔽关系缓存。
     *
     * @param userId    用户ID
     * @param blockedId 被屏蔽用户ID
     * @param isBlocked 是否屏蔽
     */
    private void updateBlockCache(long userId, long blockedId, boolean isBlocked) {
        String cacheKey = getBlockCacheKey(userId, blockedId);
        redisTemplate.opsForValue().set(cacheKey, isBlocked, 1, TimeUnit.HOURS);
    }

    /**
     * 获取屏蔽关系缓存键。
     *
     * @param userId    用户ID
     * @param blockedId 被屏蔽用户ID
     * @return 屏蔽关系缓存键
     */
    private String getBlockCacheKey(long userId, long blockedId) {
        return String.format(BLOCK_CACHE_KEY_FORMAT, userId, blockedId);
    }

    /**
     * 获取好友关系缓存键。
     *
     * @param userId   用户ID
     * @param friendId 好友ID
     * @return 好友关系缓存键
     */
    private String getFriendshipCacheKey(long userId, long friendId) {
        long smallerId = Math.min(userId, friendId);
        long largerId = Math.max(userId, friendId);
        return String.format(FRIENDSHIP_CACHE_KEY_FORMAT, smallerId, largerId);
    }
}
