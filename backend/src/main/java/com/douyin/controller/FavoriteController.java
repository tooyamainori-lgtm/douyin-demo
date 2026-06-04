package com.douyin.controller;

import com.douyin.common.PageResult;
import com.douyin.common.Result;
import com.douyin.service.FavoriteService;
import com.douyin.vo.VideoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 收藏控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * 收藏视频
     */
    @PostMapping("/videos/{id}/favorite")
    public Result<Void> add(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        favoriteService.add(id, userId);
        return Result.ok();
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/videos/{id}/favorite")
    public Result<Void> remove(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        favoriteService.remove(id, userId);
        return Result.ok();
    }

    /**
     * 我的收藏列表
     */
    @GetMapping("/users/me/favorites")
    public Result<PageResult<VideoVO>> myFavorites(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<VideoVO> result = favoriteService.listMyFavorites(userId, page, size);
        return Result.ok(result);
    }
}
