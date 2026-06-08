package com.douyin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.douyin.common.PageResult;
import com.douyin.common.Result;
import com.douyin.entity.User;
import com.douyin.entity.Video;
import com.douyin.entity.WatchHistory;
import com.douyin.mapper.UserMapper;
import com.douyin.mapper.VideoMapper;
import com.douyin.mapper.WatchHistoryMapper;
import com.douyin.vo.VideoAuthorVO;
import com.douyin.vo.VideoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 观看历史控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class HistoryController {

    private final WatchHistoryMapper watchHistoryMapper;
    private final VideoMapper videoMapper;
    private final UserMapper userMapper;

    /**
     * 我的观看历史（按最近观看时间倒序）
     */
    @GetMapping("/users/me/history")
    public Result<PageResult<VideoVO>> myHistory(
            @RequestAttribute(value = "userId", required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size) {
        if (userId == null) return Result.fail(401, "请先登录");
        Page<WatchHistory> mpPage = new Page<>(page, size);
        Page<WatchHistory> result = watchHistoryMapper.selectPage(mpPage,
                new LambdaQueryWrapper<WatchHistory>()
                        .eq(WatchHistory::getUserId, userId)
                        .orderByDesc(WatchHistory::getWatchTime));

        List<VideoVO> records = result.getRecords().stream()
                .map(h -> {
                    Video video = videoMapper.selectById(h.getVideoId());
                    if (video == null || video.getStatus() == 0) return null;
                    User author = userMapper.selectById(video.getUserId());
                    return buildVideoVO(video, author);
                })
                .filter(v -> v != null)
                .collect(Collectors.toList());

        return Result.ok(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), records));
    }

    /** 清空所有历史 */
    @DeleteMapping("/users/me/history")
    public Result<Void> clearHistory(@RequestAttribute("userId") Long userId) {
        watchHistoryMapper.deleteByUserId(userId);
        return Result.ok();
    }

    /** 删除单条历史 */
    @DeleteMapping("/users/me/history/{videoId}")
    public Result<Void> deleteHistory(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long videoId) {
        watchHistoryMapper.deleteByUserAndVideo(userId, videoId);
        return Result.ok();
    }

    private VideoVO buildVideoVO(Video video, User author) {
        return VideoVO.builder()
                .id(video.getId().toString())
                .title(video.getTitle())
                .coverUrl(video.getCoverUrl())
                .duration(video.getDuration() != null ? video.getDuration().doubleValue() : 0)
                .viewCount(video.getViewCount())
                .likeCount(video.getLikeCount())
                .createTime(video.getCreateTime() != null ? video.getCreateTime().toString() : null)
                .author(author != null ? VideoAuthorVO.builder()
                        .id(author.getId().toString())
                        .nickname(author.getNickname())
                        .avatarUrl(author.getAvatarUrl())
                        .build() : null)
                .build();
    }
}
