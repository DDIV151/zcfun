package com.ddiv.zcfun.domain.po.im;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GroupPO {
    @JsonProperty("group_id")
    private long groupId;
    @JsonProperty("group_name")
    private String groupName;
    @JsonProperty("creator_id")
    private long creatorId;
    @JsonProperty("avatar_url")
    private String avatarUrl;
    @JsonProperty("create_at")
    private LocalDateTime createAt;
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    @JsonProperty(value = "is_delete", access = JsonProperty.Access.WRITE_ONLY)
    private int isDelete;
    @JsonProperty("user_ids")
    List<Long> userIds;
}
