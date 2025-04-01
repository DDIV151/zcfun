package com.ddiv.zcfun.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginDTO {
    @JsonProperty(value = "id")
    private long userId;
    private String username;
    @JsonProperty(value = "avatar_url")
    private String avatarUrl;
    private LocalDateTime createAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
