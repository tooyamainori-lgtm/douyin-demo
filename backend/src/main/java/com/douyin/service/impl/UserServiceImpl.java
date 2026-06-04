package com.douyin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.douyin.common.exception.BusinessException;
import com.douyin.dto.LoginDTO;
import com.douyin.dto.RegisterDTO;
import com.douyin.entity.User;
import com.douyin.mapper.UserMapper;
import com.douyin.service.UserService;
import com.douyin.util.JwtUtil;
import com.douyin.vo.UserProfileVO;
import com.douyin.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

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
        return UserVO.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .token(token)
                .build();
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
        return UserVO.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .token(token)
                .build();
    }

    @Override
    public UserVO getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(1002, "用户不存在");
        }
        return UserVO.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .token(null) // 获取信息时不返回 token
                .build();
    }

    @Override
    public UserProfileVO getUserProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        // TODO: 查询关注数、粉丝数、视频数、获赞数（后续实现）
        return UserProfileVO.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .followCount(0L)
                .fansCount(0L)
                .videoCount(0L)
                .likeCount(0L)
                .createTime(user.getCreateTime() != null ? user.getCreateTime().toString() : null)
                .build();
    }
}
