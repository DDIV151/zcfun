package com.ddiv.zcfun.domain.po;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BlockPO {
    @JsonProperty(value = "user_id")
    private long userId;
    @JsonProperty(value = "blocked_id")
    private long blockedId;
    @JsonProperty(value = "create_at")
    private LocalDateTime createAt;
    @JsonProperty(value = "updated_at")
    private LocalDateTime updatedAt;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private int isDelete;
}

