package com.douyin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 观看历史实体
 */
@Data
@TableName("watch_history")
public class WatchHistory {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 视频ID */
    private Long videoId;

    /** 最近观看时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime watchTime;
}
