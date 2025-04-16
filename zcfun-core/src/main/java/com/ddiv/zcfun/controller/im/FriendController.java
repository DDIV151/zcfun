package com.ddiv.zcfun.controller.im;

import com.ddiv.zcfun.domain.ApiResult;
import com.ddiv.zcfun.domain.LoginUser;
import com.ddiv.zcfun.domain.po.im.FriendPO;
import com.ddiv.zcfun.service.FriendService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/im/friends")
@RestController
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }


    @GetMapping
    @PreAuthorize("authentication.principal.user.userId==#userId or hasRole('ADMIN')")
    public ApiResult areFriends(@RequestParam("user_id") long userId, @RequestParam("friend_id") long friendId) {
        return ApiResult.success(friendService.areFriends(userId, friendId));
    }

    @GetMapping("/list")
    @PreAuthorize("authentication.principal.user.userId==#userId or hasRole('ADMIN')")
    public ApiResult getFriends(@RequestParam("user_id") long userId) {
        List<FriendPO> friends = friendService.getFriends(userId);
        return ApiResult.success(friends);
    }

    @PostMapping("/{friend_id}")
    public ApiResult addFriend(@PathVariable("friend_id") long friendId, @AuthenticationPrincipal LoginUser loginUser) {
        friendService.addFriend(loginUser.getUser().getUserId(), friendId);
        return ApiResult.success();
    }

    @DeleteMapping("/{friend_id}")
    public ApiResult deleteFriend(@PathVariable("friend_id") long friendId, @AuthenticationPrincipal LoginUser loginUser) {
        friendService.deleteFriend(loginUser.getUser().getUserId(), friendId);
        return ApiResult.success();
    }

    @GetMapping("/block/{blocked_id}")
    public ApiResult isBlocked(@PathVariable("blocked_id") long blockedId, @AuthenticationPrincipal LoginUser loginUser) {
        return ApiResult.success(friendService.isBlocked(loginUser.getUser().getUserId(), blockedId));
    }

    @PostMapping("/block/{blocked_id}")
    @PreAuthorize("authentication.principal.user.userId==#userId or hasRole('ADMIN')")
    public ApiResult blockUser(@PathVariable("blocked_id") long blockedId, @RequestParam("user_id") long userId) {
        friendService.blockUser(userId, blockedId);
        return ApiResult.success();
    }

    @DeleteMapping("/block/{blocked_id}")
    public ApiResult unblockUser(@PathVariable("blocked_id") long blockedId, @AuthenticationPrincipal LoginUser loginUser) {
        friendService.unblockUser(loginUser.getUser().getUserId(), blockedId);
        return ApiResult.success();
    }

    @GetMapping("/blocked")
    public ApiResult getBlockedUsers(@AuthenticationPrincipal LoginUser loginUser) {
        List<FriendPO> blockedUsers = friendService.getBlockedUsers(loginUser.getUser().getUserId());
        return ApiResult.success(blockedUsers);
    }


}
