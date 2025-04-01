package com.ddiv.zcfun.domain.po;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VideoStar {
    @JsonProperty(value = "user_id")
    private long userId;
    @JsonProperty(value = "video_id")
    private long videoId;
}