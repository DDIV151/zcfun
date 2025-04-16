package com.ddiv.zcfun.controller;

import com.ddiv.zcfun.domain.ApiResult;
import com.ddiv.zcfun.domain.dto.ChangePasswordDTO;
import com.ddiv.zcfun.domain.dto.UserRegisterDTO;
import com.ddiv.zcfun.domain.po.UserPO;
import com.ddiv.zcfun.service.FriendService;
import com.ddiv.zcfun.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final FriendService friendService;

    public UserController(UserService userService, FriendService friendService) {
        this.userService = userService;
        this.friendService = friendService;
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
        if (username != null)
            return ApiResult.success(userService.getUserInfoByUsername(username));
        UserPO user = userService.getUserInfoById(id);
        return ApiResult.success(user);
    }

    /**
     * 检查用户是否屏蔽了另一个用户
     *
     * @param userId    用户ID
     * @param blockedId 被屏蔽的用户ID
     * @return ApiResult
     */
    @GetMapping("/block/check")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.user.userId==#userId")
    public ApiResult checkBlock(@RequestParam("user_id") long userId, @RequestParam("blocked_id") long blockedId) {
        boolean isBlocked = friendService.isBlocked(userId, blockedId);
        return ApiResult.success(isBlocked);
    }

    /**
     * 添加屏蔽关系
     *
     * @param userId    用户ID
     * @param blockedId 被屏蔽的用户ID
     * @return ApiResult
     */
    @PostMapping("/block")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.user.userId == #userId")
    public ApiResult addBlock(@RequestParam("user_id") long userId, @RequestParam("blocked_id") long blockedId) {
        friendService.blockUser(userId, blockedId);
        return ApiResult.success();
    }

    /**
     * 取消屏蔽关系
     *
     * @param userId    用户ID
     * @param blockedId 被屏蔽的用户ID
     * @return ApiResult
     */
    @DeleteMapping("/block")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.user.userId == #userId")
    public ApiResult removeBlock(@RequestParam("user_id") long userId, @RequestParam("blocked_id") long blockedId) {
        friendService.unblockUser(userId, blockedId);
        return ApiResult.success();
    }

}
