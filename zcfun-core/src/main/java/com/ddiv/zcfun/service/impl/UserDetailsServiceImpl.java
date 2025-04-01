package com.ddiv.zcfun.service.impl;

import com.ddiv.zcfun.domain.LoginUser;
import com.ddiv.zcfun.domain.po.UserPO;
import com.ddiv.zcfun.mapper.UserMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    public UserDetailsServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserPO user = getUserPO(username);
        return new LoginUser(user);
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
