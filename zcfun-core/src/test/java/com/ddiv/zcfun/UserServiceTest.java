package com.ddiv.zcfun;

import com.ddiv.zcfun.domain.dto.UserRegisterDTO;
import com.ddiv.zcfun.domain.po.UserPO;
import com.ddiv.zcfun.domain.po.UserRole;
import com.ddiv.zcfun.mapper.UserMapper;
import com.ddiv.zcfun.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;
    @Qualifier("objectMapper")
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void registerTest() {
        UserPO userPO = userMapper.findByUserName("test");
        if (userPO == null) {
            userService.register(new UserRegisterDTO("test", "123456"));
        }
        userPO = userMapper.findByUserName("test");
        System.out.println(userPO);
        //
        userPO = userMapper.findByUserName("admin");
        List<UserRole> userRoles = new ArrayList<>();
        userRoles.add(UserRole.ADMIN);
        userRoles.add(UserRole.USER);
        if (userPO == null) {
            UserRegisterDTO userRegisterDTO = new UserRegisterDTO("admin", "123456", userRoles);
            userService.register(userRegisterDTO);
        }
        userPO = userMapper.findByUserName("admin");
        System.out.println(userMapper.findByUserName("admin"));
    }

    @Test
    public void GetUserPO() throws Exception {
        UserPO userPO = userMapper.findByUserName("admin");
        userPO.setRole(userMapper.findUserRoleByUserId(userPO.getUserId()));
    }
}
