package com.douyin.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 视频视图对象
 */
@Data
@Builder
public class VideoVO {

    /** 视频ID */
    private String id;

    /** 视频标题 */
    private String title;

    /** 视频描述 */
    private String description;

    /** 视频播放URL */
    private String videoUrl;

    /** 封面URL */
    private String coverUrl;

    /** 视频时长（秒） */
    private Double duration;

    /** 视频宽度 */
    private Integer width;

    /** 视频高度 */
    private Integer height;

    /** 标签 */
    private String tags;

    /** 播放量 */
    private Long viewCount;

    /** 点赞数 */
    private Long likeCount;

    /** 评论数 */
    private Long commentCount;

    /** 分享数 */
    private Long shareCount;

    /** 发布时间 */
    private String createTime;

    /** 发布者信息 */
    private VideoAuthorVO author;

    /** 当前用户是否已点赞 */
    private Boolean isLiked;

    /** 当前用户是否已收藏 */
    private Boolean isFavorited;
}

