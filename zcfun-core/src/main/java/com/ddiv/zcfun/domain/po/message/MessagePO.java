package com.ddiv.zcfun.domain.po.message;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessagePO {

    private long msgId;
    private MessageType msgType;
    private String msgContent;
    private long senderId;
    private long recipientId;
    private LocalDateTime sendTime;
    private int isDelete;
    private MessageExtra msgExtra;
}
