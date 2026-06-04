package com.douyin.controller;

import com.douyin.common.Result;
import com.douyin.dto.LoginDTO;
import com.douyin.dto.RegisterDTO;
import com.douyin.service.UserService;
import com.douyin.vo.UserProfileVO;
import com.douyin.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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
     * 获取当前登录用户信息
     *
     * @param userId 从 JWT 中解析的用户ID
     * @return 用户信息
     */
    @GetMapping("/me")
    public Result<UserVO> getCurrentUser(@RequestAttribute("userId") Long userId) {
        UserVO vo = userService.getCurrentUser(userId);
        return Result.ok(vo);
    }

    /**
     * 获取用户主页
     *
     * @param id 用户ID
     * @return 用户主页信息
     */
    @GetMapping("/{id}")
    public Result<UserProfileVO> getUserProfile(@PathVariable Long id) {
        UserProfileVO vo = userService.getUserProfile(id);
        return Result.ok(vo);
    }
}
