package com.ddiv.zcfun.domain.po.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessagePO {

    @JsonProperty(value = "msg_id", access = JsonProperty.Access.WRITE_ONLY)
    private long msgId;
    @JsonProperty(value = "msg_type")
    private MessageType msgType;
    @JsonProperty(value = "msg_content")
    private String msgContent;
    @JsonProperty(value = "sender_id")
    private long senderId;
    @JsonProperty(value = "recipient_id")
    private long recipientId;
    @JsonProperty(value = "send_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;
    @JsonIgnore
    private int isDelete;
    @JsonProperty(value = "msg_extra")
    private MessageExtra msgExtra;
}
