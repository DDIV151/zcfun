package com.ddiv.zcfun.service;

import org.springframework.stereotype.Service;

@Service
public interface FriendService {
    boolean areFriends(long userA, long userB);
    public void evictFriendshipCache(long userA, long userB);
}
