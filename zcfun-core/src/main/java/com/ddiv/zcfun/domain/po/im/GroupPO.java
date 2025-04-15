package com.ddiv.zcfun.domain.po.im;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GroupPO {
    private long groupId;
    private String groupName;
    private long creatorId;
    private String avatarUrl;
    private LocalDateTime createAt;
    private LocalDateTime updatedAt;
    private int isDelete;
    List<Long> userIds;
}
