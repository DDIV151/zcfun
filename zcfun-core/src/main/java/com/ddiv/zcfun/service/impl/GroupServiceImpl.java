package com.ddiv.zcfun.service.impl;

import com.ddiv.zcfun.domain.po.GroupPO;
import com.ddiv.zcfun.exception.UserRegisterException;
import com.ddiv.zcfun.mapper.GroupMapper;
import com.ddiv.zcfun.service.GroupService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupServiceImpl implements GroupService {
    private final GroupMapper groupMapper;

    public GroupServiceImpl(GroupMapper groupMapper) {
        this.groupMapper = groupMapper;
    }

    @Override
    public boolean isGroupOwner(long userId, long groupId) {
        // 查询数据库验证用户是否是群主
        GroupPO groupPO = groupMapper.getGroupById(groupId);
        return groupPO != null && groupPO.getCreatorId() == userId;
    }

    @Override
    public void insertGroup(GroupPO group) {
        if (groupMapper.insertGroup(group) == 0)
            throw new UserRegisterException("群组创建失败");
    }

    @Override
    public void deleteGroup(long groupId) {
        groupMapper.deleteGroupById(groupId);
    }

    @Override
    public List<GroupPO> getGroupsByMemberId(long userId) {
        return groupMapper.getGroupsByMemberId(userId);
    }

}
