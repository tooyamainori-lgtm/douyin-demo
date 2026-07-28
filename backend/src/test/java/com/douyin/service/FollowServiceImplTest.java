package com.douyin.service;

import com.douyin.entity.Follow;
import com.douyin.entity.User;
import com.douyin.mapper.FollowMapper;
import com.douyin.mapper.UserMapper;
import com.douyin.service.impl.FollowServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowServiceImplTest {

    @Mock private FollowMapper followMapper;
    @Mock private UserMapper userMapper;
    @Mock private NotificationService notificationService;
    @InjectMocks private FollowServiceImpl service;

    @Test
    void duplicateFollowIsIdempotent() {
        when(userMapper.selectById(2L)).thenReturn(new User());
        when(followMapper.selectCount(any())).thenReturn(1L);

        service.follow(1L, 2L);

        verify(followMapper, never()).physicalDelete(1L, 2L);
        verify(followMapper, never()).insert(any(Follow.class));
        verifyNoInteractions(notificationService);
    }

    @Test
    void newFollowCreatesRelationAndNotification() {
        when(userMapper.selectById(2L)).thenReturn(new User());
        when(followMapper.selectCount(any())).thenReturn(0L);

        service.follow(1L, 2L);

        verify(followMapper).insert(any(Follow.class));
        verify(notificationService).create(2L, 1L, "follow", null, "关注了你");
    }
}
