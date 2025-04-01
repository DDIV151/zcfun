package com.ddiv.zcfun.service;

import com.ddiv.zcfun.domain.dto.UserRegisterDTO;
import com.ddiv.zcfun.domain.po.UserPO;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    void register(UserRegisterDTO user);

    UserPO getUserInfoById(long id);
}
