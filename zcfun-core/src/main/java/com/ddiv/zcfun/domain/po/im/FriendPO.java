package com.ddiv.zcfun.domain.po.im;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendPO {
    @JsonProperty(value = "user_id", access = JsonProperty.Access.WRITE_ONLY)
    private long userId;
    @JsonProperty(value = "friend_id")
    private long friendId;
    @JsonProperty(value = "create_at")
    private LocalDateTime createAt;
    @JsonProperty(value = "updated_at", access = JsonProperty.Access.WRITE_ONLY)
    private LocalDateTime updatedAt;
    @JsonProperty(value = "is_delete", access = JsonProperty.Access.WRITE_ONLY)
    private int isDelete = 0;
}
