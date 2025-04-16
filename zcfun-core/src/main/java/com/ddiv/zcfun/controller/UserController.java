package com.ddiv.zcfun.controller;

import com.ddiv.zcfun.domain.ApiResult;
import com.ddiv.zcfun.domain.dto.ChangePasswordDTO;
import com.ddiv.zcfun.domain.dto.UserRegisterDTO;
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

    @PostMapping("/{user_id}")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.user.userId == #id")
    public ApiResult updateUsername(@PathVariable("user_id") long id, @RequestBody UserPO user) {
        userService.updateUsername(id, user.getUsername());
        return ApiResult.success();
    }

    @PutMapping("/{user_id}/password")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.user.userId == #id")
    public ApiResult updatePassword(@PathVariable("user_id") long id, @RequestBody ChangePasswordDTO passwords) {
        userService.updatePassword(id, passwords);
        return ApiResult.success();
    }

    /**
     * @param id query参数，用户id
     * @return UserPO（当然不包含密码）
     */
    @GetMapping("/{user_id}")
    public ApiResult getInfo(@PathVariable("user_id") long id, @RequestParam(required = false, value = "username") String username) {
        if (username != null && !username.isEmpty())
            return ApiResult.success(userService.getUserInfoByUsername(username));
        UserPO user = userService.getUserInfoById(id);
        return ApiResult.success(user);
    }

}
