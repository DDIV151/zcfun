package com.ddiv.zcfun.controller.im;

import com.ddiv.zcfun.domain.ApiResult;
import com.ddiv.zcfun.domain.LoginUser;
import com.ddiv.zcfun.domain.po.im.GroupPO;
import com.ddiv.zcfun.service.GroupService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.getUser().getUserId()")
    public ApiResult getGroupsByMemberId(@RequestParam("user_id") long userId) {
        return ApiResult.success(groupService.getGroupsByMemberId(userId));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ApiResult createGroup(@RequestBody GroupPO group, @AuthenticationPrincipal LoginUser loginUser) {
        group.setCreatorId(loginUser.getUser().getUserId());
        groupService.insertGroup(group);
        return ApiResult.success();
    }

    @PreAuthorize("hasRole('ADMIN') or @groupService.isGroupOwner(authentication.principal.getUser().getUserId(), #groupId)")
    public ApiResult updateGroup(@RequestBody GroupPO group) {
        return ApiResult.success();
    }

    @PreAuthorize("hasRole('ADMIN') or @groupService.isGroupOwner(authentication.principal.getUser().getUserId(), #groupId)")
    public ApiResult deleteGroup(@RequestParam("group_id") long groupId) {
        groupService.deleteGroup(groupId);
        return ApiResult.success();
    }


    public ApiResult addUserToGroup(@RequestParam("group_id") long groupId, @RequestParam("user_id") long userId) {
        return ApiResult.success();
    }

    public ApiResult removeUserFromGroup() {
        return ApiResult.success();
    }
}