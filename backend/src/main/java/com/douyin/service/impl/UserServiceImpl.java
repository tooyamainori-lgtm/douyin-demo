package com.douyin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.douyin.common.exception.BusinessException;
import com.douyin.dto.LoginDTO;
import com.douyin.dto.RegisterDTO;
import com.douyin.dto.UpdateProfileDTO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.douyin.entity.User;
import com.douyin.entity.Video;
import com.douyin.mapper.FollowMapper;
import com.douyin.mapper.UserMapper;
import com.douyin.mapper.VideoMapper;
import com.douyin.service.FollowService;
import com.douyin.service.UserService;
import com.douyin.util.JwtUtil;
import com.douyin.util.MinioUtil;
import com.douyin.vo.UserProfileVO;
import com.douyin.vo.UserStatsVO;
import com.douyin.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final VideoMapper videoMapper;
    private final FollowMapper followMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final MinioUtil minioUtil;
    private final FollowService followService;

    @Override
    public UserVO register(RegisterDTO dto) {
        // 1. 检查用户名是否已存在
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException(1001, "用户名已存在");
        }

        // 2. 构建用户实体
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname("用户_" + System.currentTimeMillis() % 10000000000L); // 默认昵称
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(1); // 正常状态

        // 3. 保存到数据库
        userMapper.insert(user);
        log.info("新用户注册：{}", user.getUsername());

        // 4. 生成 JWT Token
        String token = jwtUtil.generateToken(user.getId());

        // 5. 组装返回
        return buildUserVOWithToken(user, token);
    }

    @Override
    public UserVO login(LoginDTO dto) {
        // 1. 查询用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (user == null) {
            throw new BusinessException(1002, "用户名或密码错误");
        }

        // 2. 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(1002, "用户名或密码错误");
        }

        // 3. 检查账号状态
        if (user.getStatus() == 0) {
            throw new BusinessException(1003, "账号已被禁用");
        }

        // 4. 生成 JWT Token
        String token = jwtUtil.generateToken(user.getId());
        log.info("用户登录：{}", user.getUsername());

        // 5. 组装返回
        return buildUserVOWithToken(user, token);
    }

    @Override
    public UserVO getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(1002, "用户不存在");
        }
        return buildUserVO(user);
    }

    @Override
    public UserProfileVO getUserProfile(Long userId, Long currentUserId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        // 真实统计数据
        long followCount = followService.countFollowing(userId);
        long fansCount = followService.countFollowers(userId);
        long videoCount = videoMapper.selectCount(
                new LambdaQueryWrapper<Video>().eq(Video::getUserId, userId).eq(Video::getStatus, 1));
        Long likeCount = videoMapper.getTotalLikesByUserId(userId);
        boolean isFollowing = followService.isFollowing(currentUserId, userId);

        return UserProfileVO.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .followCount(followCount)
                .fansCount(fansCount)
                .videoCount(videoCount)
                .likeCount(likeCount != null ? likeCount : 0L)
                .isFollowing(isFollowing)
                .createTime(user.getCreateTime() != null ? user.getCreateTime().toString() : null)
                .build();
    }

    @Override
    public UserVO updateProfile(Long userId, UpdateProfileDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getBio() != null) {
            user.setBio(dto.getBio());
        }
        if (dto.getGender() != null) {
            user.setGender(dto.getGender());
        }
        if (dto.getBirthday() != null) {
            user.setBirthday(LocalDate.parse(dto.getBirthday()));
        }
        userMapper.updateById(user);
        return buildUserVO(user);
    }

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        try {
            String objectName = minioUtil.uploadImage(file, "avatars");
            String avatarUrl = minioUtil.getPublicUrl(objectName);
            user.setAvatarUrl(avatarUrl);
            userMapper.updateById(user);
            log.info("用户 {} 头像更新成功：{}", userId, avatarUrl);
            return avatarUrl;
        } catch (Exception e) {
            log.error("头像上传失败", e);
            throw new BusinessException(2002, "上传失败，请重试");
        }
    }

    @Override
    public UserStatsVO getUserStats(Long userId) {
        Long videoCount = videoMapper.selectCount(
                new LambdaQueryWrapper<Video>().eq(Video::getUserId, userId).eq(Video::getStatus, 1));
        Long totalViews = videoMapper.getTotalViewsByUserId(userId);
        Long totalLikes = videoMapper.getTotalLikesByUserId(userId);
        Long totalComments = videoMapper.getTotalCommentsByUserId(userId);
        Long fansCount = followService.countFollowers(userId);
        Long recentFansGrowth = followMapper.countRecentFollowers(userId, 7);

        return UserStatsVO.builder()
                .totalViews(totalViews != null ? totalViews : 0L)
                .totalLikes(totalLikes != null ? totalLikes : 0L)
                .totalComments(totalComments != null ? totalComments : 0L)
                .videoCount(videoCount != null ? videoCount : 0L)
                .fansCount(fansCount)
                .recentFansGrowth(recentFansGrowth != null ? recentFansGrowth : 0L)
                .build();
    }

    private UserVO buildUserVO(User user) {
        return UserVO.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .gender(user.getGender())
                .birthday(user.getBirthday() != null ? user.getBirthday().toString() : null)
                .build();
    }

    private UserVO buildUserVOWithToken(User user, String token) {
        return UserVO.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .gender(user.getGender())
                .birthday(user.getBirthday() != null ? user.getBirthday().toString() : null)
                .token(token)
                .build();
    }
}
