package com.ddiv.zcfun.service;

import com.ddiv.zcfun.domain.po.im.GroupPO;

import java.util.List;
import java.util.Set;

public interface GroupService {

    void deleteGroup(long groupId);

    List<GroupPO> getGroupsByMemberId(long userId);

    boolean isGroupOwner(long userId, long groupId);

    void insertGroup(GroupPO group);

    void updateGroup(GroupPO group);

    void addUserToGroup(long groupId, long userId);

    void removeUserFromGroup(long groupId, long userId);

    public Set<Long> getAllGroupMembers(Long groupId);

    public Set<Long> getGroupOnlineMembers(Long groupId);
}
