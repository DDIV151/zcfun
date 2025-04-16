package com.ddiv.zcfun.service.impl;

import com.ddiv.zcfun.domain.LoginUser;
import com.ddiv.zcfun.domain.po.UserPO;
import com.ddiv.zcfun.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public UserDetailsServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserPO user;
        LoginUser loginUser;
        // 从缓存中获取用户信息
        user = (UserPO) redisTemplate.opsForValue().get("userPO:" + username);
        if (user != null) {
            return new LoginUser(user);
        }

        // 缓存中没有，从数据库获取
        user = getUserPO(username);
        loginUser = new LoginUser(user);

        // 将用户信息存入缓存，设置过期时间为1小时
        redisTemplate.opsForValue().set("userPO:" + username, user, 12, TimeUnit.HOURS);

        return loginUser;
    }

    private UserPO getUserPO(String username) {
        UserPO user = userMapper.findByUserName(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        user.setRole(userMapper.findUserRoleByUserId(user.getUserId()));
        return user;
    }
}