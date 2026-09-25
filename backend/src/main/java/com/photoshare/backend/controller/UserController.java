package com.photoshare.backend.controller;

import com.photoshare.backend.common.Result;
import com.photoshare.backend.dto.UserBriefDTO;
import com.photoshare.backend.dto.UserProfileDTO;
import com.photoshare.backend.security.SecurityUtils;
import com.photoshare.backend.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "用户与关注")
public class UserController {

    @Autowired
    private FollowService followService;

    @GetMapping("/suggest")
    @Operation(summary = "用户候选（@提及选人面板与添加关注共用）")
    public Result<List<UserBriefDTO>> suggest(@RequestParam(required = false) String keyword) {
        return Result.success(followService.suggestUsers(keyword, SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "用户主页：资料、统计与查看者可见的相册")
    public Result<UserProfileDTO> profile(@PathVariable String id) {
        return Result.success(followService.getProfile(id, SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/{id}/followers")
    @Operation(summary = "粉丝列表")
    public Result<List<UserBriefDTO>> followers(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(followService.listFollowers(id, SecurityUtils.getCurrentUserId(), page, size));
    }

    @GetMapping("/{id}/following")
    @Operation(summary = "关注列表")
    public Result<List<UserBriefDTO>> following(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(followService.listFollowing(id, SecurityUtils.getCurrentUserId(), page, size));
    }

    @PostMapping("/{id}/follow")
    @Operation(summary = "关注用户（重复关注不报错）")
    public Result<Void> follow(@PathVariable String id) {
        followService.follow(id, SecurityUtils.getCurrentUserId());
        return Result.success();
    }

    @DeleteMapping("/{id}/follow")
    @Operation(summary = "取消关注")
    public Result<Void> unfollow(@PathVariable String id) {
        followService.unfollow(id, SecurityUtils.getCurrentUserId());
        return Result.success();
    }
}
