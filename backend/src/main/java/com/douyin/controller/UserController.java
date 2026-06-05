package com.douyin.controller;

import com.douyin.common.PageResult;
import com.douyin.common.Result;
import com.douyin.dto.LoginDTO;
import com.douyin.dto.RegisterDTO;
import com.douyin.dto.UpdateProfileDTO;
import com.douyin.service.UserService;
import com.douyin.service.VideoService;
import com.douyin.vo.UserProfileVO;
import com.douyin.vo.UserVO;
import com.douyin.vo.VideoVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final VideoService videoService;

    /**
     * 用户注册
     *
     * @param dto 注册参数
     * @return 用户信息 + Token
     */
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterDTO dto) {
        UserVO vo = userService.register(dto);
        return Result.ok(vo);
    }

    /**
     * 用户登录
     *
     * @param dto 登录参数
     * @return 用户信息 + Token
     */
    @PostMapping("/login")
    public Result<UserVO> login(@Valid @RequestBody LoginDTO dto) {
        UserVO vo = userService.login(dto);
        return Result.ok(vo);
    }

    /**
     * 获取当前登录用户信息（需携带 JWT Token）
     *
     * @param userId 从 JWT 中解析的用户ID（未登录时为 null）
     * @return 用户信息
     */
    @GetMapping("/me")
    public Result<UserVO> getCurrentUser(@RequestAttribute(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }
        UserVO vo = userService.getCurrentUser(userId);
        return Result.ok(vo);
    }

    /**
     * 我点赞的视频列表
     */
    @GetMapping("/me/likes")
    public Result<PageResult<VideoVO>> myLikes(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<VideoVO> result = videoService.getMyLikes(userId, page, size);
        return Result.ok(result);
    }

    /**
     * 获取用户主页
     *
     * @param id 用户ID
     * @return 用户主页信息
     */
    @GetMapping("/{id}")
    public Result<UserProfileVO> getUserProfile(
            @PathVariable Long id,
            @RequestAttribute(value = "userId", required = false) Long currentUserId) {
        UserProfileVO vo = userService.getUserProfile(id, currentUserId);
        return Result.ok(vo);
    }

    /**
     * 获取用户发布的视频
     *
     * @param id   用户ID
     * @param page 页码
     * @param size 每页条数
     * @return 视频分页
     */
    @GetMapping("/{id}/videos")
    public Result<PageResult<VideoVO>> getUserVideos(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<VideoVO> result = videoService.getUserVideos(id, page, size);
        return Result.ok(result);
    }

    /**
     * 更新个人资料
     *
     * @param dto    更新参数
     * @param userId 当前用户ID
     * @return 更新后的用户信息
     */
    @PutMapping("/me")
    public Result<UserVO> updateProfile(
            @RequestBody UpdateProfileDTO dto,
            @RequestAttribute("userId") Long userId) {
        UserVO vo = userService.updateProfile(userId, dto);
        return Result.ok(vo);
    }

    /**
     * 上传头像
     *
     * @param file   头像文件
     * @param userId 当前用户ID
     * @return 头像URL
     */
    @PostMapping("/me/avatar")
    public Result<String> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestAttribute("userId") Long userId) {
        String url = userService.uploadAvatar(userId, file);
        return Result.ok(url);
    }
}
