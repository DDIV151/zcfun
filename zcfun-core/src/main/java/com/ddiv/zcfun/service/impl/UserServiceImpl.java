package com.ddiv.zcfun.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.ddiv.zcfun.domain.dto.ChangePasswordDTO;
import com.ddiv.zcfun.domain.dto.UserRegisterDTO;
import com.ddiv.zcfun.domain.po.UserPO;
import com.ddiv.zcfun.domain.po.UserRole;
import com.ddiv.zcfun.exception.UserRegisterException;
import com.ddiv.zcfun.mapper.UserMapper;
import com.ddiv.zcfun.service.UserService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service("userService")
public class UserServiceImpl implements UserService {


    private final UserMapper userMapper;
    private final Snowflake snowflake;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;


    public UserServiceImpl(UserMapper userMapper, Snowflake snowflake, PasswordEncoder passwordEncoder, RedisTemplate<String, Object> redisTemplate) {
        this.userMapper = userMapper;
        this.snowflake = snowflake;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(UserRegisterDTO user) {
        //检查参数合法性（不为空）
        String username = user.getUsername();
        String password = user.getPassword();
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            throw new UserRegisterException("用户名或密码为空");
        }
        if (userMapper.findByUserName(username) != null)
            throw new UserRegisterException("用户名重复");

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
        }
    }

    @Override
    public UserPO getUserInfoById(long id) {
        UserPO user = userMapper.findByUserId(id);
        if (user == null)
            return null;
        user.setRole(userMapper.findUserRoleByUserId(id));
        return user;
    }

    @Override
    public boolean checkUserExists(long id) {
        return userMapper.findByUserId(id) != null;
    }

    @Override
    public Object getUserInfoByUsername(String username) {
        UserPO user = userMapper.findByUserName(username);
        if (user == null)
            return null;
        user.setRole(userMapper.findUserRoleByUserId(user.getUserId()));
        return user;
    }

    @Override
    public void updateUsername(long id, String username) {
        if (username == null || username.isEmpty())
            throw new IllegalArgumentException("用户名不能为空");
        try {
            evictUserCache(userMapper.findByUserId(id).getUsername());
            userMapper.updateUsername(id, username);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            throw new UserRegisterException("用户名重复");
        }
    }

    @Override
    public void updatePassword(long id, ChangePasswordDTO password) {
        if (password.getOldPassword().isEmpty() || password.getNewPassword().isEmpty())
            throw new IllegalArgumentException("密码不能为空");
        UserPO userPO = userMapper.findByUserId(id);
        if (!passwordEncoder.matches(password.getOldPassword(), userPO.getPassword()))
            throw new IllegalArgumentException("旧密码错误");
        userMapper.updatePassword(id, passwordEncoder.encode(password.getNewPassword()));
        evictUserCache(userPO.getUsername());
    }

    public void evictUserCache(String username) {
        redisTemplate.delete("userPO:" + username);
        redisTemplate.delete("user:token:" + username);
    }
}
