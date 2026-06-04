package com.douyin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.douyin.common.PageResult;
import com.douyin.common.exception.BusinessException;
import com.douyin.entity.User;
import com.douyin.entity.Video;
import com.douyin.mapper.UserMapper;
import com.douyin.mapper.VideoMapper;
import com.douyin.service.VideoService;
import com.douyin.vo.VideoAuthorVO;
import com.douyin.vo.VideoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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

    @Value("${douyin.upload.path:uploads}")
    private String uploadPath;

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

        // 2. 保存文件到本地
        String fileExt = ".mp4";
        String fileName = UUID.randomUUID().toString().replace("-", "") + fileExt;
        Path videoDir = Paths.get(uploadPath, "videos");
        try {
            Files.createDirectories(videoDir);
            Path targetPath = videoDir.resolve(fileName);
            file.transferTo(targetPath.toFile());
            log.info("视频文件已保存：{}", targetPath);
        } catch (IOException e) {
            log.error("视频保存失败", e);
            throw new BusinessException(2002, "上传失败，请重试");
        }

        // 3. 构建视频实体
        Video video = new Video();
        video.setUserId(userId);
        video.setTitle(title);
        video.setDescription(description != null ? description : "");
        video.setVideoUrl("/uploads/videos/" + fileName);
        video.setCoverUrl(""); // TODO: 后续用 FFmpeg 生成封面
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
        // TODO: 检查当前用户是否已点赞
        return buildVideoVO(video, author, false);
    }

    /**
     * 构建 VideoVO
     */
    private VideoVO buildVideoVO(Video video, User author, boolean isLiked) {
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
                .build();
    }
}
