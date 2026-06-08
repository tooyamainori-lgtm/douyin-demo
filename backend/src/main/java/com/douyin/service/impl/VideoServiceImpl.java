package com.douyin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.douyin.common.PageResult;
import com.douyin.common.exception.BusinessException;
import com.douyin.entity.Follow;
import com.douyin.entity.LikeRecord;
import com.douyin.entity.User;
import com.douyin.entity.Video;
import com.douyin.entity.VideoFavorite;
import com.douyin.entity.WatchHistory;
import com.douyin.mapper.FollowMapper;
import com.douyin.mapper.LikeRecordMapper;
import com.douyin.mapper.VideoFavoriteMapper;
import com.douyin.mapper.UserMapper;
import com.douyin.mapper.VideoMapper;
import com.douyin.mapper.WatchHistoryMapper;
import com.douyin.service.VideoService;
import io.minio.DownloadObjectArgs;
import io.minio.MinioClient;

import com.douyin.service.NotificationService;
import com.douyin.util.MinioUtil;
import com.douyin.util.RedisUtil;
import com.douyin.vo.TagVO;
import com.douyin.vo.VideoAuthorVO;
import com.douyin.vo.VideoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
    private final FollowMapper followMapper;
    private final WatchHistoryMapper watchHistoryMapper;
    private final RedisUtil redisUtil;
    private final MinioUtil minioUtil;
    private final NotificationService notificationService;

    @Value("${douyin.ffmpeg.path:D:/APP/ffmpeg/bin/ffmpeg.exe}")
    private String ffmpegPath;
    @Value("${minio.bucket}")
    private String minioBucket;

    private final MinioClient minioClient;

    @Override
    public VideoVO upload(MultipartFile file, String title, String description, String tags, MultipartFile cover, Long userId) {
        // 1. 校验文件
        if (file.isEmpty()) {
            throw new BusinessException(400, "请选择视频文件");
        }
        String originalName = file.getOriginalFilename();
        if (originalName != null && !originalName.toLowerCase().endsWith(".mp4")) {
            throw new BusinessException(400, "仅支持 MP4 格式");
        }

        // 2. 保存视频到临时文件
        String fileExt = ".mp4";
        String tmpName = "douyin-upload-" + UUID.randomUUID().toString().replace("-", "") + fileExt;
        Path tmpVideo = null;
        String objectName;
        try {
            tmpVideo = Files.createTempFile("douyin-", fileExt);
            file.transferTo(tmpVideo.toFile());
        } catch (Exception e) {
            log.error("视频临时保存失败", e);
            throw new BusinessException(2002, "上传失败");
        }

        // 3. 上传视频到 MinIO
        try {
            objectName = "videos/" + UUID.randomUUID().toString().replace("-", "") + fileExt;
            minioUtil.uploadFile(tmpVideo.toString(), objectName, "video/mp4");
            log.info("视频已上传到 MinIO：{}", objectName);
        } catch (Exception e) {
            log.error("视频上传到 MinIO 失败", e);
            throw new BusinessException(2002, "上传失败，请重试");
        }

        // 4. 封面：用户上传 > FFmpeg 自动截帧 > 默认空
        String coverUrl = "";
        if (cover != null && !cover.isEmpty()) {
            try {
                String coverName = minioUtil.uploadImage(cover, "covers");
                coverUrl = minioUtil.getPublicUrl(coverName);
            } catch (Exception e) {
                log.warn("用户封面上传失败", e);
            }
        } else {
            // FFmpeg 自动截帧
            coverUrl = extractCover(tmpVideo.toString());
        }

        // 5. 清理临时文件
        try { Files.deleteIfExists(tmpVideo); } catch (Exception ignored) {}

        // 6. 构建视频实体
        Video video = new Video();
        video.setUserId(userId);
        video.setTitle(title);
        video.setDescription(description != null ? description : "");
        video.setVideoUrl(minioUtil.getPublicUrl(objectName));
        video.setCoverUrl(coverUrl);
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

        // 关键词搜索（按标题模糊匹配），同时记录到热搜
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Video::getTitle, keyword);
            redisUtil.zIncrBy("hot_search", keyword, 1);
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
    public void recordView(Long videoId, Long userId) {
        Video video = videoMapper.selectById(videoId);
        if (video != null) {
            video.setViewCount(video.getViewCount() + 1);
            videoMapper.updateById(video);
            // 更新热门排行榜：播放 +1
            redisUtil.addHotScore(videoId, 1);
        }
        // 记录观看历史（已登录用户）
        if (userId != null) {
            WatchHistory history = new WatchHistory();
            history.setUserId(userId);
            history.setVideoId(videoId);
            // 已存在则更新 watch_time，不存在则插入
            WatchHistory existing = watchHistoryMapper.selectOne(
                    new LambdaQueryWrapper<WatchHistory>()
                            .eq(WatchHistory::getUserId, userId)
                            .eq(WatchHistory::getVideoId, videoId));
            if (existing != null) {
                existing.setWatchTime(java.time.LocalDateTime.now());
                watchHistoryMapper.updateById(existing);
            } else {
                history.setWatchTime(java.time.LocalDateTime.now());
                watchHistoryMapper.insert(history);
            }
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
            // 通知视频作者
            notificationService.create(video.getUserId(), userId, "like", videoId, "点赞了你的视频");
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
    public PageResult<VideoVO> getMyLikes(Long userId, Integer page, Integer size) {
        // 分页查询点赞记录
        Page<LikeRecord> mpPage = new Page<>(page, size);
        Page<LikeRecord> likePage = likeRecordMapper.selectPage(mpPage,
                new LambdaQueryWrapper<LikeRecord>()
                        .eq(LikeRecord::getUserId, userId)
                        .orderByDesc(LikeRecord::getCreateTime));

        List<VideoVO> records = likePage.getRecords().stream()
                .map(lr -> {
                    Video video = videoMapper.selectById(lr.getVideoId());
                    if (video == null || video.getStatus() == 0) return null;
                    User author = userMapper.selectById(video.getUserId());
                    return buildVideoVO(video, author, true);
                })
                .filter(v -> v != null)
                .collect(Collectors.toList());

        return PageResult.of(likePage.getTotal(), likePage.getCurrent(), likePage.getSize(), records);
    }

    @Override
    public int batchGenerateCovers() {
        List<Video> videos = videoMapper.selectWithoutCover();
        int count = 0;
        for (Video video : videos) {
            String videoUrl = video.getVideoUrl();
            if (videoUrl == null || !videoUrl.contains("/douyin/")) continue;
            // 从 MinIO URL 提取 object name
            String objectName = videoUrl.substring(videoUrl.indexOf("/douyin/") + "/douyin/".length());
            // 下载到临时文件
            Path tmpVideo = null;
            try {
                tmpVideo = Files.createTempFile("cover-", ".mp4");
                Files.deleteIfExists(tmpVideo); // 确保干净
                minioClient.downloadObject(
                        DownloadObjectArgs.builder()
                                .bucket(minioBucket)
                                .object(objectName)
                                .filename(tmpVideo.toString())
                                .build());
                // FFmpeg 截帧
                String coverUrl = extractCover(tmpVideo.toString());
                if (!coverUrl.isEmpty()) {
                    video.setCoverUrl(coverUrl);
                    videoMapper.updateById(video);
                    count++;
                    log.info("批量封面: videoId={} cover={}", video.getId(), coverUrl);
                }
            } catch (Exception e) {
                log.warn("批量封面失败: videoId={}", video.getId(), e);
            } finally {
                if (tmpVideo != null) try { Files.deleteIfExists(tmpVideo); } catch (Exception ignored) {}
            }
        }
        return count;
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

    @Override
    public PageResult<VideoVO> getFollowingFeed(Long userId, Integer page, Integer size) {
        // 查询关注的用户ID列表
        List<Long> followeeIds = followMapper.selectList(
                new LambdaQueryWrapper<Follow>()
                        .eq(Follow::getFollowerId, userId)
                        .select(Follow::getFolloweeId)
        ).stream().map(Follow::getFolloweeId).toList();

        if (followeeIds.isEmpty()) {
            return new PageResult<>(0L, (long) page, (long) size, List.of());
        }

        // 分页查询这些用户的视频，按时间倒序
        Page<Video> mpPage = new Page<>(page, size);
        Page<Video> result = videoMapper.selectPage(mpPage,
                new LambdaQueryWrapper<Video>()
                        .in(Video::getUserId, followeeIds)
                        .orderByDesc(Video::getCreateTime));

        List<VideoVO> records = result.getRecords().stream().map(v -> {
            User author = userMapper.selectById(v.getUserId());
            boolean isLiked = false;
            if (userId != null) {
                isLiked = likeRecordMapper.selectCount(
                        new LambdaQueryWrapper<LikeRecord>()
                                .eq(LikeRecord::getVideoId, v.getId())
                                .eq(LikeRecord::getUserId, userId)) > 0;
            }
            return buildVideoVO(v, author, isLiked);
        }).collect(Collectors.toList());

        return new PageResult<>(result.getTotal(), (long) page, (long) size, records);
    }

    // ======================== 预设标签 ========================

    /** 预设标签定义：name → emoji */
    private static final java.util.Map<String, String> PRESET_TAGS = java.util.Map.<String, String>ofEntries(
            java.util.Map.entry("搞笑", "😂"),
            java.util.Map.entry("舞蹈", "💃"),
            java.util.Map.entry("音乐", "🎵"),
            java.util.Map.entry("美食", "🍜"),
            java.util.Map.entry("旅行", "✈️"),
            java.util.Map.entry("萌宠", "🐶"),
            java.util.Map.entry("运动", "⚽"),
            java.util.Map.entry("颜值", "✨"),
            java.util.Map.entry("知识", "📚"),
            java.util.Map.entry("科技", "🚀"),
            java.util.Map.entry("汽车", "🚗"),
            java.util.Map.entry("时尚", "👗"),
            java.util.Map.entry("二次元", "🎮"),
            java.util.Map.entry("影视", "🎬"),
            java.util.Map.entry("生活记录", "📝")
    );

    @Override
    public List<TagVO> listTags() {
        return PRESET_TAGS.entrySet().stream()
                .map(entry -> {
                    Long count = videoMapper.selectCount(
                            new LambdaQueryWrapper<Video>()
                                    .eq(Video::getStatus, 1)
                                    .like(Video::getTags, entry.getKey()));
                    return TagVO.builder()
                            .name(entry.getKey())
                            .videoCount(count)
                            .icon(entry.getValue())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<VideoVO> getByTag(String tagName, Integer page, Integer size) {
        Page<Video> mpPage = new Page<>(page, size);
        Page<Video> result = videoMapper.selectPage(mpPage,
                new LambdaQueryWrapper<Video>()
                        .eq(Video::getStatus, 1)
                        .like(Video::getTags, tagName)
                        .orderByDesc(Video::getCreateTime));

        List<VideoVO> records = result.getRecords().stream()
                .map(v -> {
                    User user = userMapper.selectById(v.getUserId());
                    return buildVideoVO(v, user, false);
                })
                .collect(Collectors.toList());

        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    /**
     * 构建 VideoVO
     */
    private VideoVO buildVideoVO(Video video, User author, boolean isLiked) {
        return buildVideoVO(video, author, isLiked, false);
    }

    /** FFmpeg 自动截取视频帧作为封面 */
    private String extractCover(String videoPath) {
        try {
            Path coverTmp = Files.createTempFile("douyin-cover-", ".png");
            ProcessBuilder pb = new ProcessBuilder(
                    ffmpegPath,
                    "-ss", "0.5",        // 提前 seek 更快，0.5s 兼容短视频
                    "-i", videoPath,
                    "-vframes", "1",
                    "-update", "1",       // 单帧输出
                    "-y",
                    coverTmp.toString()
            );
            pb.redirectErrorStream(true);
            Process p = pb.start();
            if (!p.waitFor(10, java.util.concurrent.TimeUnit.SECONDS)) {
                p.destroyForcibly();
                Files.deleteIfExists(coverTmp);
                log.warn("FFmpeg 超时");
                return "";
            }

            if (coverTmp.toFile().length() > 0) {
                String coverName = "covers/" + UUID.randomUUID().toString().replace("-", "") + ".png";
                minioUtil.uploadFile(coverTmp.toString(), coverName, "image/png");
                Files.deleteIfExists(coverTmp);
                log.info("FFmpeg 自动封面生成成功：{}", coverName);
                return minioUtil.getPublicUrl(coverName);
            }
            Files.deleteIfExists(coverTmp);
        } catch (Exception e) {
            log.warn("FFmpeg 自动封面失败", e);
        }
        return "";
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
