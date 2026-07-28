package com.douyin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.douyin.common.exception.BusinessException;
import com.douyin.entity.Follow;
import com.douyin.entity.User;
import com.douyin.mapper.FollowMapper;
import com.douyin.mapper.UserMapper;
import com.douyin.service.FollowService;
import com.douyin.service.NotificationService;
import com.douyin.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 关注服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowMapper followMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void follow(Long userId, Long targetId) {
        if (userId.equals(targetId)) {
            throw new BusinessException(400, "不能关注自己");
        }
        if (userMapper.selectById(targetId) == null) {
            throw new BusinessException(404, "用户不存在");
        }
        // 重复关注按幂等成功处理，不重复写入关系和通知。
        Long count = followMapper.selectCount(
                new LambdaQueryWrapper<Follow>()
                        .eq(Follow::getFollowerId, userId)
                        .eq(Follow::getFolloweeId, targetId));
        if (count > 0) {
            return;
        }
        Follow follow = new Follow();
        follow.setFollowerId(userId);
        follow.setFolloweeId(targetId);
        followMapper.insert(follow);
        notificationService.create(targetId, userId, "follow", null, "关注了你");
        log.info("用户 {} 关注了 {}", userId, targetId);
    }

    @Override
    @Transactional
    public void unfollow(Long userId, Long targetId) {
        int deleted = followMapper.physicalDelete(userId, targetId);
        if (deleted == 0) {
            throw new BusinessException(3004, "未关注该用户");
        }
        log.info("用户 {} 取关了 {}", userId, targetId);
    }

    @Override
    public boolean isFollowing(Long userId, Long targetId) {
        if (userId == null || targetId == null) return false;
        return followMapper.selectCount(
                new LambdaQueryWrapper<Follow>()
                        .eq(Follow::getFollowerId, userId)
                        .eq(Follow::getFolloweeId, targetId)) > 0;
    }

    @Override
    public List<UserVO> getFollowing(Long userId) {
        List<Follow> follows = followMapper.selectList(
                new LambdaQueryWrapper<Follow>()
                        .eq(Follow::getFollowerId, userId)
                        .orderByDesc(Follow::getCreateTime));
        if (follows.isEmpty()) return List.of();
        // 批量查询用户，解决 N+1 问题
        List<Long> userIds = follows.stream().map(Follow::getFolloweeId).distinct().collect(Collectors.toList());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        return follows.stream()
                .map(f -> toVO(userMap.get(f.getFolloweeId())))
                .filter(v -> v != null).collect(Collectors.toList());
    }

    @Override
    public List<UserVO> getFollowers(Long userId) {
        List<Follow> follows = followMapper.selectList(
                new LambdaQueryWrapper<Follow>()
                        .eq(Follow::getFolloweeId, userId)
                        .orderByDesc(Follow::getCreateTime));
        if (follows.isEmpty()) return List.of();
        // 批量查询用户，解决 N+1 问题
        List<Long> userIds = follows.stream().map(Follow::getFollowerId).distinct().collect(Collectors.toList());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        return follows.stream()
                .map(f -> toVO(userMap.get(f.getFollowerId())))
                .filter(v -> v != null).collect(Collectors.toList());
    }

    @Override
    public long countFollowing(Long userId) {
        return followMapper.selectCount(
                new LambdaQueryWrapper<Follow>()
                        .eq(Follow::getFollowerId, userId));
    }

    @Override
    public long countFollowers(Long userId) {
        return followMapper.selectCount(
                new LambdaQueryWrapper<Follow>()
                        .eq(Follow::getFolloweeId, userId));
    }

    private UserVO toVO(User u) {
        return UserVO.builder()
                .id(u.getId().toString())
                .username(u.getUsername())
                .nickname(u.getNickname())
                .avatarUrl(u.getAvatarUrl())
                .build();
    }
}
