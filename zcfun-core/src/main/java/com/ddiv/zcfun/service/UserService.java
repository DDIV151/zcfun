package com.ddiv.zcfun.service;

import com.ddiv.zcfun.domain.dto.ChangePasswordDTO;
import com.ddiv.zcfun.domain.dto.UserRegisterDTO;
import com.ddiv.zcfun.domain.po.UserPO;


public interface UserService {

    void register(UserRegisterDTO user);

    UserPO getUserInfoById(long id);

    boolean checkUserExists(long id);

    Object getUserInfoByUsername(String username);

    void updateUsername(long id, String username);

    void updatePassword(long id, ChangePasswordDTO password);
}
