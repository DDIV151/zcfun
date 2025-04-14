package com.ddiv.zcfun.controller;

import com.ddiv.zcfun.domain.ApiResult;
import com.ddiv.zcfun.domain.dto.UserRegisterDTO;
import com.ddiv.zcfun.domain.po.GroupPO;
import com.ddiv.zcfun.domain.po.UserPO;
import com.ddiv.zcfun.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * @param user UserDTO,username与password（未加密）
     * @return 10000成功
     */
    @PostMapping("/register")
    public ApiResult register(@RequestBody UserRegisterDTO user) {
        userService.register(user);
        return ApiResult.success();
    }

    /**
     * @param id query参数，用户id
     * @return UserPO（当然不包含密码）
     */
    @GetMapping("/info")
    @PreAuthorize("hasRole('USER')||hasRole('ADMIN')")
    public ApiResult getInfo(@RequestParam("user_id") long id) {
        UserPO user = userService.getUserInfoById(id);
        return ApiResult.success(user);
    }
}
