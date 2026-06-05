package com.douyin.controller;

import com.douyin.common.Result;
import com.douyin.service.FollowService;
import com.douyin.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 关注控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    /**
     * 关注用户
     */
    @PostMapping("/follows/{userId}")
    public Result<Void> follow(
            @PathVariable Long userId,
            @RequestAttribute("userId") Long currentUserId) {
        followService.follow(currentUserId, userId);
        return Result.ok();
    }

    /**
     * 取消关注
     */
    @DeleteMapping("/follows/{userId}")
    public Result<Void> unfollow(
            @PathVariable Long userId,
            @RequestAttribute("userId") Long currentUserId) {
        followService.unfollow(currentUserId, userId);
        return Result.ok();
    }

    /**
     * 关注列表（我关注的人）
     */
    @GetMapping("/users/{userId}/following")
    public Result<List<UserVO>> getFollowing(@PathVariable Long userId) {
        return Result.ok(followService.getFollowing(userId));
    }

    /**
     * 粉丝列表（关注我的人）
     */
    @GetMapping("/users/{userId}/followers")
    public Result<List<UserVO>> getFollowers(@PathVariable Long userId) {
        return Result.ok(followService.getFollowers(userId));
    }

    /**
     * 是否已关注
     */
    @GetMapping("/follows/{userId}/status")
    public Result<Boolean> isFollowing(
            @PathVariable Long userId,
            @RequestAttribute(value = "userId", required = false) Long currentUserId) {
        return Result.ok(followService.isFollowing(currentUserId, userId));
    }
}
