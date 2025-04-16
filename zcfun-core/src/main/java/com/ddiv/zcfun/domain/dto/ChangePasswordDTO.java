package com.ddiv.zcfun.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ChangePasswordDTO {
    @JsonProperty(value = "old_password")
    private String oldPassword;
    @JsonProperty(value = "new_password")
    private String newPassword;
}
