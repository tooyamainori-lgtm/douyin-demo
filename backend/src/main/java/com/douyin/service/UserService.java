package com.douyin.service;

import com.douyin.dto.LoginDTO;
import com.douyin.dto.RegisterDTO;
import com.douyin.vo.UserProfileVO;
import com.douyin.vo.UserVO;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     *
     * @param dto 注册参数
     * @return 用户信息 + Token
     */
    UserVO register(RegisterDTO dto);

    /**
     * 用户登录
     *
     * @param dto 登录参数
     * @return 用户信息 + Token
     */
    UserVO login(LoginDTO dto);

    /**
     * 获取当前登录用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserVO getCurrentUser(Long userId);

    /**
     * 获取用户主页信息
     *
     * @param userId 用户ID
     * @return 用户主页信息
     */
    UserProfileVO getUserProfile(Long userId);
}
