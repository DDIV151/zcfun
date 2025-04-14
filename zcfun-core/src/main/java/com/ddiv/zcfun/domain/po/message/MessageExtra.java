package com.ddiv.zcfun.domain.po.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageExtra {
    @JsonProperty(value = "msg_id")
    private long msgId;
    @JsonProperty(value = "recall_user_id")
    private long recallUserId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty(value = "recall_time")
    private LocalDateTime recallTime;
    @JsonProperty(value = "extension")
    private String extension;
    @JsonIgnore
    private int isDelete;
}
