package com.ddiv.zcfun.domain.po.message;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageExtra {
    private long msgId;
    private long recallUserId;
    private LocalDateTime recallTime;
    private String extension;
    private int isDelete;
}
