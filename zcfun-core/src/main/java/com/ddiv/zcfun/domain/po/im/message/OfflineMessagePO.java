package com.ddiv.zcfun.domain.po.im.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OfflineMessagePO {
    private long id;
    @JsonProperty("recipient_id")
    private long recipientId;
    private String content;
    @JsonProperty("create_at")
    private LocalDateTime create_at;
    @JsonProperty("updated_at")
    private LocalDateTime updated_at;
    @JsonProperty(value = "is_delete", access = JsonProperty.Access.WRITE_ONLY)
    private int isDelete;
}
