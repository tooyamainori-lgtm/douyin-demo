package com.douyin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.douyin.common.PageResult;
import com.douyin.common.exception.BusinessException;
import com.douyin.entity.User;
import com.douyin.entity.Video;
import com.douyin.entity.VideoFavorite;
import com.douyin.mapper.UserMapper;
import com.douyin.mapper.VideoFavoriteMapper;
import com.douyin.mapper.VideoMapper;
import com.douyin.service.FavoriteService;
import com.douyin.vo.VideoAuthorVO;
import com.douyin.vo.VideoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 收藏服务实现
 */
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final VideoFavoriteMapper favoriteMapper;
    private final VideoMapper videoMapper;
    private final UserMapper userMapper;

    @Override
    public void add(Long videoId, Long userId) {
        Long count = favoriteMapper.selectCount(
                new LambdaQueryWrapper<VideoFavorite>()
                        .eq(VideoFavorite::getUserId, userId)
                        .eq(VideoFavorite::getVideoId, videoId));
        if (count > 0) {
            throw new BusinessException(400, "已经收藏过了");
        }
        if (videoMapper.selectById(videoId) == null) {
            throw new BusinessException(2001, "视频不存在");
        }
        VideoFavorite fav = new VideoFavorite();
        fav.setUserId(userId);
        fav.setVideoId(videoId);
        favoriteMapper.insert(fav);
    }

    @Override
    public void remove(Long videoId, Long userId) {
        favoriteMapper.physicalDelete(userId, videoId);
    }

    @Override
    public PageResult<VideoVO> listMyFavorites(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<VideoFavorite> wrapper = new LambdaQueryWrapper<VideoFavorite>()
                .eq(VideoFavorite::getUserId, userId)
                .orderByDesc(VideoFavorite::getCreateTime);

        Page<VideoFavorite> mpPage = new Page<>(page, size);
        Page<VideoFavorite> result = favoriteMapper.selectPage(mpPage, wrapper);

        List<VideoVO> records = result.getRecords().stream()
                .map(fav -> {
                    Video video = videoMapper.selectById(fav.getVideoId());
                    if (video == null) return null;
                    User author = userMapper.selectById(video.getUserId());
                    return VideoVO.builder()
                            .id(video.getId().toString())
                            .title(video.getTitle())
                            .description(video.getDescription())
                            .videoUrl(video.getVideoUrl())
                            .coverUrl(video.getCoverUrl())
                            .duration(video.getDuration() != null ? video.getDuration().doubleValue() : 0)
                            .width(video.getWidth())
                            .height(video.getHeight())
                            .tags(video.getTags())
                            .viewCount(video.getViewCount())
                            .likeCount(video.getLikeCount())
                            .commentCount(video.getCommentCount())
                            .shareCount(video.getShareCount())
                            .createTime(video.getCreateTime() != null ? video.getCreateTime().toString() : null)
                            .author(author != null ? VideoAuthorVO.builder()
                                    .id(author.getId().toString())
                                    .nickname(author.getNickname())
                                    .avatarUrl(author.getAvatarUrl())
                                    .build() : null)
                            .isLiked(false)
                            .build();
                })
                .filter(v -> v != null)
                .collect(Collectors.toList());

        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    @Override
    public boolean isFavorited(Long videoId, Long userId) {
        if (userId == null) return false;
        return favoriteMapper.selectCount(
                new LambdaQueryWrapper<VideoFavorite>()
                        .eq(VideoFavorite::getUserId, userId)
                        .eq(VideoFavorite::getVideoId, videoId)) > 0;
    }
}
