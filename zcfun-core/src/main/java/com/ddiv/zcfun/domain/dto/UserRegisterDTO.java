package com.ddiv.zcfun.domain.dto;

import com.ddiv.zcfun.domain.po.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterDTO {
    private String username;
    private String password;
    private List<UserRole> role;

    public UserRegisterDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
