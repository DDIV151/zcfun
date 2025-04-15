package com.ddiv.zcfun.service;

import com.ddiv.zcfun.domain.po.im.GroupPO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface GroupService {

    void deleteGroup(long groupId);

    List<GroupPO> getGroupsByMemberId(long userId);

    boolean isGroupOwner(long userId, long groupId);

    void insertGroup(GroupPO group);
}
