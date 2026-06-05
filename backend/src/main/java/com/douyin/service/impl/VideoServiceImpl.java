package com.douyin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.douyin.common.PageResult;
import com.douyin.common.exception.BusinessException;
import com.douyin.entity.LikeRecord;
import com.douyin.entity.User;
import com.douyin.entity.Video;
import com.douyin.entity.VideoFavorite;
import com.douyin.mapper.LikeRecordMapper;
import com.douyin.mapper.VideoFavoriteMapper;
import com.douyin.mapper.UserMapper;
import com.douyin.mapper.VideoMapper;
import com.douyin.service.VideoService;
import com.douyin.util.MinioUtil;
import com.douyin.util.RedisUtil;
import com.douyin.vo.VideoAuthorVO;
import com.douyin.vo.VideoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 视频服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    private final VideoMapper videoMapper;
    private final UserMapper userMapper;
    private final LikeRecordMapper likeRecordMapper;
    private final VideoFavoriteMapper favoriteMapper;
    private final RedisUtil redisUtil;
    private final MinioUtil minioUtil;

    @Override
    public VideoVO upload(MultipartFile file, String title, String description, String tags, Long userId) {
        // 1. 校验文件
        if (file.isEmpty()) {
            throw new BusinessException(400, "请选择视频文件");
        }
        String originalName = file.getOriginalFilename();
        if (originalName != null && !originalName.toLowerCase().endsWith(".mp4")) {
            throw new BusinessException(400, "仅支持 MP4 格式");
        }

        // 2. 上传文件到 MinIO
        String objectName;
        try {
            objectName = minioUtil.uploadVideo(file);
        } catch (Exception e) {
            log.error("视频上传到 MinIO 失败", e);
            throw new BusinessException(2002, "上传失败，请重试");
        }

        // 3. 构建视频实体
        Video video = new Video();
        video.setUserId(userId);
        video.setTitle(title);
        video.setDescription(description != null ? description : "");
        video.setVideoUrl(minioUtil.getPublicUrl(objectName));
        video.setDuration(java.math.BigDecimal.ZERO); // TODO: 后续用 FFmpeg 提取时长
        video.setWidth(0);
        video.setHeight(0);
        video.setSize(file.getSize());
        video.setTags(tags != null ? tags : "");
        video.setStatus(1); // 正常
        video.setViewCount(0L);
        video.setLikeCount(0L);
        video.setCommentCount(0L);
        video.setShareCount(0L);

        // 4. 写入数据库
        videoMapper.insert(video);

        // 5. 组装返回
        User user = userMapper.selectById(userId);
        return buildVideoVO(video, user, false);
    }

    @Override
    public PageResult<VideoVO> page(Integer page, Integer size, String keyword) {
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<Video>()
                .eq(Video::getStatus, 1) // 只查正常状态的视频
                .orderByDesc(Video::getCreateTime);

        // 关键词搜索（按标题模糊匹配）
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Video::getTitle, keyword);
        }

        Page<Video> mpPage = new Page<>(page, size);
        Page<Video> result = videoMapper.selectPage(mpPage, wrapper);

        // 组装 VO
        List<VideoVO> records = result.getRecords().stream()
                .map(v -> {
                    User user = userMapper.selectById(v.getUserId());
                    return buildVideoVO(v, user, false);
                })
                .collect(Collectors.toList());

        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    @Override
    public VideoVO getDetail(Long videoId, Long userId) {
        Video video = videoMapper.selectById(videoId);
        if (video == null || video.getStatus() == 0) {
            throw new BusinessException(2001, "视频不存在");
        }
        User author = userMapper.selectById(video.getUserId());
        // 检查当前用户是否已点赞
        boolean isLiked = false;
        boolean isFavorited = false;
        if (userId != null) {
            isLiked = likeRecordMapper.selectCount(
                    new LambdaQueryWrapper<LikeRecord>()
                            .eq(LikeRecord::getUserId, userId)
                            .eq(LikeRecord::getVideoId, videoId)) > 0;
            isFavorited = favoriteMapper.selectCount(
                    new LambdaQueryWrapper<VideoFavorite>()
                            .eq(VideoFavorite::getUserId, userId)
                            .eq(VideoFavorite::getVideoId, videoId)) > 0;
        }
        return buildVideoVO(video, author, isLiked, isFavorited);
    }

    @Override
    public void recordView(Long videoId) {
        Video video = videoMapper.selectById(videoId);
        if (video != null) {
            video.setViewCount(video.getViewCount() + 1);
            videoMapper.updateById(video);
            // 更新热门排行榜：播放 +1
            redisUtil.addHotScore(videoId, 1);
        }
    }

    @Override
    public void like(Long videoId, Long userId) {
        // 检查是否已存在活跃的点赞记录
        Long count = likeRecordMapper.selectCount(
                new LambdaQueryWrapper<LikeRecord>()
                        .eq(LikeRecord::getUserId, userId)
                        .eq(LikeRecord::getVideoId, videoId));
        if (count > 0) {
            throw new BusinessException(3002, "已经点过赞了");
        }
        // 插入点赞记录
        LikeRecord record = new LikeRecord();
        record.setUserId(userId);
        record.setVideoId(videoId);
        likeRecordMapper.insert(record);
        // 更新视频点赞数
        Video video = videoMapper.selectById(videoId);
        if (video != null) {
            video.setLikeCount(video.getLikeCount() + 1);
            videoMapper.updateById(video);
            // 更新热门排行榜：点赞 +3
            redisUtil.addHotScore(videoId, 3);
        }
    }

    @Override
    public void unlike(Long videoId, Long userId) {
        // 物理删除点赞记录
        likeRecordMapper.physicalDelete(userId, videoId);
        // 更新视频点赞数（确保不小于0）
        Video video = videoMapper.selectById(videoId);
        if (video != null && video.getLikeCount() > 0) {
            video.setLikeCount(video.getLikeCount() - 1);
            videoMapper.updateById(video);
            // 取消点赞热度 -3
            redisUtil.addHotScore(videoId, -3);
        }
    }

    @Override
    public PageResult<VideoVO> getUserVideos(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<Video>()
                .eq(Video::getUserId, userId)
                .eq(Video::getStatus, 1)
                .orderByDesc(Video::getCreateTime);

        Page<Video> mpPage = new Page<>(page, size);
        Page<Video> result = videoMapper.selectPage(mpPage, wrapper);

        User author = userMapper.selectById(userId);
        List<VideoVO> records = result.getRecords().stream()
                .map(v -> buildVideoVO(v, author, false))
                .collect(Collectors.toList());

        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    @Override
    public void delete(Long videoId, Long userId) {
        Video video = videoMapper.selectById(videoId);
        if (video == null || video.getStatus() == 0) {
            throw new BusinessException(2001, "视频不存在");
        }
        if (!video.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除他人视频");
        }

        // 从 MinIO 删除文件
        String videoUrl = video.getVideoUrl();
        if (videoUrl != null && videoUrl.contains("/douyin/")) {
            int idx = videoUrl.indexOf("/douyin/") + "/douyin/".length();
            String objectName = videoUrl.substring(idx);
            minioUtil.delete(objectName);
        }

        // 逻辑删除数据库记录
        videoMapper.deleteById(videoId);
        log.info("视频已删除：videoId={}, userId={}", videoId, userId);
    }

    @Override
    public List<VideoVO> getHotRank(int top) {
        Map<Long, Double> hotMap = redisUtil.getHotRank(top);
        return hotMap.entrySet().stream()
                .map(entry -> {
                    Video video = videoMapper.selectById(entry.getKey());
                    if (video == null) return null;
                    User author = userMapper.selectById(video.getUserId());
                    return buildVideoVO(video, author, false);
                })
                .filter(v -> v != null)
                .collect(Collectors.toList());
    }

    /**
     * 构建 VideoVO
     */
    private VideoVO buildVideoVO(Video video, User author, boolean isLiked) {
        return buildVideoVO(video, author, isLiked, false);
    }

    private VideoVO buildVideoVO(Video video, User author, boolean isLiked, boolean isFavorited) {
        VideoAuthorVO authorVO = null;
        if (author != null) {
            authorVO = VideoAuthorVO.builder()
                    .id(author.getId().toString())
                    .nickname(author.getNickname())
                    .avatarUrl(author.getAvatarUrl())
                    .build();
        }

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
                .author(authorVO)
                .isLiked(isLiked)
                .isFavorited(isFavorited)
                .build();
    }
}
