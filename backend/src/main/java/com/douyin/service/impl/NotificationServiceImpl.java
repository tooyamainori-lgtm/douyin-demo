package com.douyin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.douyin.common.PageResult;
import com.douyin.entity.Notification;
import com.douyin.entity.User;
import com.douyin.mapper.NotificationMapper;
import com.douyin.mapper.UserMapper;
import com.douyin.service.NotificationService;
import com.douyin.vo.NotificationUserVO;
import com.douyin.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;

    @Override
    public void create(Long userId, Long fromUserId, String type, Long videoId, String content) {
        if (userId.equals(fromUserId)) return; // 不给自己发通知
        Notification n = new Notification();
        n.setUserId(userId);
        n.setFromUserId(fromUserId);
        n.setType(type);
        n.setVideoId(videoId);
        n.setContent(content);
        n.setIsRead(false);
        notificationMapper.insert(n);
    }

    @Override
    public PageResult<NotificationVO> list(Long userId, Integer page, Integer size) {
        Page<Notification> mpPage = new Page<>(page, size);
        Page<Notification> result = notificationMapper.selectPage(mpPage,
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .orderByDesc(Notification::getCreateTime));

        List<NotificationVO> records = result.getRecords().stream().map(n -> {
            User from = userMapper.selectById(n.getFromUserId());
            return NotificationVO.builder()
                    .id(n.getId().toString())
                    .type(n.getType())
                    .content(n.getContent())
                    .isRead(n.getIsRead())
                    .videoId(n.getVideoId() != null ? n.getVideoId().toString() : null)
                    .createTime(n.getCreateTime().toString())
                    .fromUser(from != null ? NotificationUserVO.builder()
                            .id(from.getId().toString())
                            .nickname(from.getNickname())
                            .avatarUrl(from.getAvatarUrl())
                            .build() : null)
                    .build();
        }).collect(Collectors.toList());

        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    @Override
    public void markAllRead(Long userId) {
        List<Notification> list = notificationMapper.selectList(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, false));
        list.forEach(n -> n.setIsRead(true));
        list.forEach(notificationMapper::updateById);
    }

    @Override
    public long countUnread(Long userId) {
        return notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, false));
    }
}
