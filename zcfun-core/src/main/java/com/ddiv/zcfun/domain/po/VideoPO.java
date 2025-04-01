package com.ddiv.zcfun.domain.po;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VideoPO {
    @JsonProperty(value = "video_id")
    long videoId;
    String title;
    @JsonProperty(value = "video_url")
    String videoUrl;
    long views;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY, value = "is_delete")
    int isDelete;
    @JsonProperty(value = "create_at")
    LocalDateTime createAt;
    @JsonProperty(value = "updated_at")
    LocalDateTime updatedAt;
}
