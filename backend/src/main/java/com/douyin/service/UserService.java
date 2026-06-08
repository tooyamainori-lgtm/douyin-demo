package com.douyin.service;

import com.douyin.dto.LoginDTO;
import com.douyin.dto.RegisterDTO;
import com.douyin.dto.UpdateProfileDTO;
import com.douyin.vo.UserProfileVO;
import com.douyin.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

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
     * @param userId        用户ID
     * @param currentUserId 当前登录用户ID（可选）
     * @return 用户主页信息
     */
    UserProfileVO getUserProfile(Long userId, Long currentUserId);

    /**
     * 更新个人资料
     *
     * @param userId 用户ID
     * @param dto    更新参数
     * @return 更新后的用户信息
     */
    UserVO updateProfile(Long userId, UpdateProfileDTO dto);

    /**
     * 上传头像
     *
     * @param userId 用户ID
     * @param file   头像文件
     * @return 头像URL
     */
    String uploadAvatar(Long userId, MultipartFile file);

    /**
     * 用户数据统计
     *
     * @param userId 用户ID
     * @return 统计数据
     */
    com.douyin.vo.UserStatsVO getUserStats(Long userId);
}
