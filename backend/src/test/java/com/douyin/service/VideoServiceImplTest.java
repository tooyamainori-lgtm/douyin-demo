package com.douyin.service;

import com.douyin.common.exception.BusinessException;
import com.douyin.entity.LikeRecord;
import com.douyin.entity.Video;
import com.douyin.entity.WatchHistory;
import com.douyin.mapper.*;
import com.douyin.service.impl.VideoServiceImpl;
import com.douyin.util.MinioUtil;
import com.douyin.util.RedisUtil;
import io.minio.MinioClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoServiceImplTest {

    @Mock private VideoMapper videoMapper;
    @Mock private UserMapper userMapper;
    @Mock private LikeRecordMapper likeRecordMapper;
    @Mock private VideoFavoriteMapper favoriteMapper;
    @Mock private FollowMapper followMapper;
    @Mock private WatchHistoryMapper watchHistoryMapper;
    @Mock private RedisUtil redisUtil;
    @Mock private MinioUtil minioUtil;
    @Mock private NotificationService notificationService;
    @Mock private MinioClient minioClient;
    @InjectMocks private VideoServiceImpl service;

    @Test
    void unlikeDoesNotDecreaseCountWhenLikeDoesNotExist() {
        when(likeRecordMapper.physicalDelete(1L, 10L)).thenReturn(0);

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.unlike(10L, 1L));

        assertEquals(3006, error.getCode());
        verify(videoMapper, never()).decrementLikeCount(anyLong());
        verify(redisUtil, never()).addHotScore(anyLong(), anyDouble());
    }

    @Test
    void unlikeDecreasesCountAfterDeletingLike() {
        when(likeRecordMapper.physicalDelete(1L, 10L)).thenReturn(1);

        service.unlike(10L, 1L);

        verify(videoMapper).decrementLikeCount(10L);
        verify(redisUtil).addHotScore(10L, -3);
    }

    @Test
    void likeRejectsMissingVideoBeforeCreatingRecord() {
        when(videoMapper.selectById(10L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> service.like(10L, 1L));

        verify(likeRecordMapper, never()).insert(any(LikeRecord.class));
    }

    @Test
    void recordViewRejectsMissingVideo() {
        when(videoMapper.selectById(10L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> service.recordView(10L, 1L));

        verify(videoMapper, never()).incrementViewCount(anyLong());
        verify(watchHistoryMapper, never()).insert(any(WatchHistory.class));
    }
}
