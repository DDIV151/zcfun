package com.ddiv.zcfun.controller.im;

import com.ddiv.zcfun.domain.ApiResult;
import com.ddiv.zcfun.domain.LoginUser;
import com.ddiv.zcfun.domain.po.im.GroupPO;
import com.ddiv.zcfun.service.GroupService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/im")
@RestController
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping("/users/{user_id}/groups")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.getUser().getUserId()")
    public ApiResult getGroupsByMemberId(@PathVariable("user_id") long userId) {
        return ApiResult.success(groupService.getGroupsByMemberId(userId));
    }

    @PostMapping("/groups")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ApiResult createGroup(@RequestBody GroupPO group, @AuthenticationPrincipal LoginUser loginUser) {
        group.setCreatorId(loginUser.getUser().getUserId());
        groupService.insertGroup(group);
        return ApiResult.success();
    }

    @PutMapping("/groups/{groupId}")
    @PreAuthorize("hasRole('ADMIN') or @groupService.isGroupOwner(authentication.principal.user.userId , #groupId)")
    public ApiResult updateGroup(@RequestBody GroupPO group, @PathVariable("groupId") long groupId) {
        group.setGroupId(groupId);
        groupService.updateGroup(group);
        return ApiResult.success();
    }

    @DeleteMapping("/groups/{group_id}")
    @PreAuthorize("hasRole('ADMIN') or @groupService.isGroupOwner(authentication.principal.getUser().getUserId() , #groupId)")
    public ApiResult deleteGroup(@PathVariable("group_id") long groupId) {
        groupService.deleteGroup(groupId);
        return ApiResult.success();
    }


    @PostMapping("/groups/{group_id}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.user.userId or @groupService.isGroupOwner(authentication.principal.user.userId, #groupId)")
    public ApiResult addUserToGroup(@PathVariable("group_id") long groupId, @RequestParam("user_id") long userId) {
        groupService.addUserToGroup(groupId, userId);
        return ApiResult.success();
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.user.userId or @groupService.isGroupOwner(authentication.principal.user.userId, #groupId)")
    @DeleteMapping("/groups/{group_id}/users/{user_id}")
    public ApiResult removeUserFromGroup(@PathVariable("group_id") long groupId, @PathVariable("user_id") long userId) {
        groupService.removeUserFromGroup(groupId, userId);
        return ApiResult.success();
    }
}