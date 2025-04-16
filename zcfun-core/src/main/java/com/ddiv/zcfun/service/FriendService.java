package com.ddiv.zcfun.service;

import com.ddiv.zcfun.domain.po.im.FriendPO;

import java.util.List;

public interface FriendService {
    boolean areFriends(long userA, long userB);

    public void evictFriendshipCache(long userA, long userB);

    public boolean isBlocked(long userId, long blockedId);

    public void blockUser(long userId, long blockedId);

    public void unblockUser(long userId, long blockedId);

    public void evictBlockCache(long userId, long blockedId);

    List<FriendPO> getFriends(long userId);

    void addFriend(long userId, long friendId);

    void deleteFriend(long userId, long friendId);

    List<FriendPO> getBlockedUsers(long userId);
}
