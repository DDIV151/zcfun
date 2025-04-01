package com.ddiv.zcfun.domain.po;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPO {
    @JsonProperty(value = "user_id")
    private long userId;
    private String username;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    @JsonProperty(value = "avatar_url")
    private String avatarUrl;
    @JsonProperty(value = "create_at")
    private LocalDateTime createAt;
    @JsonProperty(value = "updated_at")
    private LocalDateTime updatedAt;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private int isDelete;
    private List<UserRole> role;
}
