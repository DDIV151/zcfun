package com.ddiv.zcfun.domain.po.im;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class FriendPO {
    @JsonProperty(value = "user_id")
    long userId;
    @JsonProperty(value = "friend_id")
    long friendId;
    @JsonProperty(value = "create_at")
    LocalDateTime createAt;
    @JsonProperty(value = "updated_at")
    LocalDateTime updatedAt;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    int isDelete;
}
