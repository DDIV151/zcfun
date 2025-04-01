package com.ddiv.zcfun.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.ddiv.zcfun.domain.dto.UserRegisterDTO;
import com.ddiv.zcfun.domain.po.UserPO;
import com.ddiv.zcfun.domain.po.UserRole;
import com.ddiv.zcfun.exception.UserRegisterException;
import com.ddiv.zcfun.mapper.UserMapper;
import com.ddiv.zcfun.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class UserServiceImpl implements UserService {


    private final UserMapper userMapper;
    private final Snowflake snowflake;
    private final PasswordEncoder passwordEncoder;


    public UserServiceImpl(UserMapper userMapper, Snowflake snowflake, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.snowflake = snowflake;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void register(UserRegisterDTO user) {
        //检查参数合法性（不为空）
        String username = user.getUsername();
        String password = user.getPassword();
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            throw new UserRegisterException("用户名或密码为空");
        }

        //封装为PO对象:
        UserPO userPO = new UserPO();
        //用户名
        userPO.setUsername(username);
        //加密密码
        userPO.setPassword(passwordEncoder.encode(password));
        //id
        long id = snowflake.nextId();
        userPO.setUserId(id);
        //角色（权限）
        List<UserRole> roles = new ArrayList<>();
        roles.add(UserRole.USER);
        userPO.setRole(user.getRole() == null ? roles : user.getRole());
        try {
            roles = userPO.getRole();
            userMapper.addUser(userPO);
            for (UserRole role : roles) {
                userMapper.addRole(id, role);
            }
        } catch (org.springframework.dao.DuplicateKeyException e) {
            throw new UserRegisterException("用户名重复");
        } catch (Exception e) {
            userMapper.cancelUserAdd(userPO);
            throw e;
        }
    }

    @Override
    public UserPO getUserInfoById(long id) {
        UserPO user = userMapper.findByUserId(id);
        user.setRole(userMapper.findUserRoleByUserId(id));
        return user;
    }


}
