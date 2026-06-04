package com.douyin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 视频表实体
 */
@Data
@TableName("video")
public class Video {

    /** 主键ID（雪花算法） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 发布者用户ID */
    private Long userId;

    /** 视频标题 */
    private String title;

    /** 视频描述 */
    private String description;

    /** 视频文件URL */
    private String videoUrl;

    /** 封面图片URL */
    private String coverUrl;

    /** 视频时长（秒） */
    private BigDecimal duration;

    /** 视频宽度（像素） */
    private Integer width;

    /** 视频高度（像素） */
    private Integer height;

    /** 文件大小（字节） */
    private Long size;

    /** 标签，逗号分隔 */
    private String tags;

    /** 状态：0-审核不通过 1-正常 2-转码中 */
    private Integer status;

    /** 播放量 */
    private Long viewCount;

    /** 点赞数（冗余字段） */
    private Long likeCount;

    /** 评论数（冗余字段） */
    private Long commentCount;

    /** 分享数（冗余字段） */
    private Long shareCount;

    /** 发布时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除 */
    @TableLogic
    private Integer isDeleted;
}
