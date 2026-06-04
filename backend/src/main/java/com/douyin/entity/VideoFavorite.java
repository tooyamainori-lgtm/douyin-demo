package com.douyin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 视频收藏实体
 */
@Data
@TableName("video_favorite")
public class VideoFavorite {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private Long videoId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer isDeleted;
}
