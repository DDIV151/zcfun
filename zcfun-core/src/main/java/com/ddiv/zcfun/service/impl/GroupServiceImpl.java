package com.ddiv.zcfun.service.impl;

import com.ddiv.zcfun.domain.po.im.GroupPO;
import com.ddiv.zcfun.exception.UserRegisterException;
import com.ddiv.zcfun.mapper.GroupMapper;
import com.ddiv.zcfun.service.GroupService;
import com.ddiv.zcfun.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service("groupService")
public class GroupServiceImpl implements GroupService {
    private static final Logger log = LoggerFactory.getLogger(GroupServiceImpl.class);
    private final GroupMapper groupMapper; // 群组数据访问对象
    private final UserService userService; // 用户服务
    private final RedisTemplate<String, Object> redisTemplate; // Redis缓存模板
    private static final String GROUP_CACHE_KEY = "group:%d"; // 群组缓存键格式

    public GroupServiceImpl(GroupMapper groupMapper, UserService userService, RedisTemplate<String, Object> redisTemplate) {
        this.groupMapper = groupMapper;
        this.userService = userService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean isGroupOwner(long userId, long groupId) {
        GroupPO group = getGroupById(groupId);
        return group != null && group.getCreatorId() == userId; // 判断用户是否为群组创建者
    }

    @Transactional
    @Override
    public void insertGroup(GroupPO group) {
        // 检查创建者是否存在
        checkUserExists(group.getCreatorId());
        if (groupMapper.insertGroup(group) == 0) {
            throw new UserRegisterException("群组创建失败");
        }
        // 添加创建者到群组
        groupMapper.insertUserToGroup(group.getGroupId(), group.getCreatorId());
        // 缓存群组信息
        cacheGroup(group);
    }

    @Transactional
    @Override
    public void updateGroup(GroupPO group) {
        if (groupMapper.updateGroup(group) == 0) {
            throw new UserRegisterException("群组更新失败");
        }
        evictGroupCache(group.getGroupId()); // 清除旧缓存
        cacheGroup(group); // 更新后重新缓存
    }

    @Override
    public void addUserToGroup(long groupId, long userId) {
        checkUserExists(userId);
        checkGroupExists(groupId);
        if (groupMapper.getGroupById(groupId).getUserIds().contains(userId))
            throw new UserRegisterException("用户已加入群组");
        groupMapper.insertUserToGroup(groupId, userId);
        evictGroupCache(groupId); // 成员变动，清除群组缓存
    }

    @Override
    public void removeUserFromGroup(long groupId, long userId) {
        checkUserExists(userId);
        checkGroupExists(groupId);
        if (groupMapper.getGroupById(groupId).getCreatorId() == userId)
            throw new UserRegisterException("群主不能退出群组");
        if (groupMapper.removeUserFromGroup(userId, groupId) == 0) {
            throw new UserRegisterException("用户不在群组中或移除失败");
        }
        evictGroupCache(groupId); // 成员变动，清除缓存
    }

    @Transactional
    @Override
    public void deleteGroup(long groupId) {
        groupMapper.deleteGroupById(groupId);
        groupMapper.deleteGroupUserById(groupId);
        groupMapper.deleteGroupMessageById(groupId);
        groupMapper.deleteGroupMessageExtraById(groupId);
        evictGroupCache(groupId);
    }

    @Override
    public List<GroupPO> getGroupsByMemberId(long userId) {
        checkUserExists(userId);
        return groupMapper.getGroupsByMemberId(userId); // 获取用户所在的群组列表
    }

    private GroupPO getGroupById(long groupId) {
        String cacheKey = String.format(GROUP_CACHE_KEY, groupId);
        GroupPO group = (GroupPO) redisTemplate.opsForValue().get(cacheKey);
        if (group != null) {
            return group; // 从缓存中获取群组信息
        }
        group = groupMapper.getGroupById(groupId);
        if (group != null && group.getIsDelete() == 0) {
            redisTemplate.opsForValue().set(cacheKey, group, 1, TimeUnit.HOURS); // 缓存群组信息
        }
        return group;
    }

    private void checkUserExists(long userId) {
        if (!userService.checkUserExists(userId)) {
            throw new IllegalArgumentException("用户不存在"); // 检查用户是否存在
        }
    }

    private void checkGroupExists(long groupId) {
        GroupPO group = getGroupById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("群组不存在"); // 检查群组是否存在
        }
    }

    private void cacheGroup(GroupPO group) {
        String cacheKey = String.format(GROUP_CACHE_KEY, group.getGroupId());
        redisTemplate.opsForValue().set(cacheKey, group, 1, TimeUnit.HOURS); // 缓存群组信息
    }

    private void evictGroupCache(long groupId) {
        String cacheKey = String.format(GROUP_CACHE_KEY, groupId);
        redisTemplate.delete(cacheKey); // 清除群组缓存
    }
}
